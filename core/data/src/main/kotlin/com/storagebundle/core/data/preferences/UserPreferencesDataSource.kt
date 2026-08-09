package com.storagebundle.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User settings backed by DataStore.
 *
 * @property hasCompletedOnboarding whether the user has seen the "what this app can see"
 *   screen. Onboarding explains the permission model before any permission is requested.
 * @property duplicateStrictness Hamming-distance threshold for treating two images as
 *   duplicates. Conservative by default — see [DEFAULT_DUPLICATE_STRICTNESS].
 * @property trashInsteadOfDelete whether deletions go to the system trash rather than being
 *   permanent. Defaults to `true`; permanent deletion is opt-in (PLAN.md §5.2).
 */
data class UserPreferences(
    val hasCompletedOnboarding: Boolean,
    val duplicateStrictness: Int,
    val trashInsteadOfDelete: Boolean,
)

/** Reads and writes [UserPreferences]. */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** Emits the current preferences, and again on every change. */
    val preferences: Flow<UserPreferences> = dataStore.data.map { stored ->
        UserPreferences(
            hasCompletedOnboarding = stored[KEY_ONBOARDING_COMPLETE] ?: false,
            duplicateStrictness = stored[KEY_DUPLICATE_STRICTNESS] ?: DEFAULT_DUPLICATE_STRICTNESS,
            trashInsteadOfDelete = stored[KEY_TRASH_INSTEAD_OF_DELETE] ?: true,
        )
    }

    /** Records that onboarding has been completed. */
    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = complete }
    }

    /**
     * Sets the duplicate-detection threshold, clamped to a sane range.
     *
     * Values above [MAX_DUPLICATE_STRICTNESS] produce false positives, and a false positive
     * here means a user loses a real photo — the app's single Critical risk (PLAN.md §11).
     */
    suspend fun setDuplicateStrictness(threshold: Int) {
        val clamped = threshold.coerceIn(MIN_DUPLICATE_STRICTNESS, MAX_DUPLICATE_STRICTNESS)
        dataStore.edit { it[KEY_DUPLICATE_STRICTNESS] = clamped }
    }

    /** Sets whether deletions are routed through the system trash. */
    suspend fun setTrashInsteadOfDelete(useTrash: Boolean) {
        dataStore.edit { it[KEY_TRASH_INSTEAD_OF_DELETE] = useTrash }
    }

    /** Threshold bounds for duplicate detection, in Hamming distance over a 64-bit hash. */
    companion object {
        /** Only near-identical images match. */
        const val MIN_DUPLICATE_STRICTNESS: Int = 2

        /** Default from PLAN.md §5.2 — deliberately conservative. */
        const val DEFAULT_DUPLICATE_STRICTNESS: Int = 10

        /** Beyond this, visually distinct images start to collide. */
        const val MAX_DUPLICATE_STRICTNESS: Int = 16

        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        private val KEY_DUPLICATE_STRICTNESS = intPreferencesKey("duplicate_strictness")
        private val KEY_TRASH_INSTEAD_OF_DELETE = booleanPreferencesKey("trash_instead_of_delete")
    }
}
