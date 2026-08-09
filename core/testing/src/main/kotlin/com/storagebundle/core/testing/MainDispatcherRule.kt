package com.storagebundle.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces the Main dispatcher with a deterministic test dispatcher.
 *
 * ViewModels collect on Main; without this rule those tests either hang or fail with
 * "Module with the Main dispatcher had failed to initialize".
 *
 * @param testDispatcher the dispatcher to install. [UnconfinedTestDispatcher] runs coroutines
 *   eagerly, which suits assertions on emitted state; pass a `StandardTestDispatcher` when a
 *   test needs to control advancement explicitly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
