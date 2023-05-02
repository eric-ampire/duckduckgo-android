/*
 * Copyright (c) 2023 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.savedsites.impl.sync

import com.duckduckgo.savedsites.api.SavedSitesRepository
import com.duckduckgo.sync.api.SyncCrypto
import com.duckduckgo.sync.api.engine.SyncChanges
import com.duckduckgo.sync.api.engine.SyncMergeResult
import com.duckduckgo.sync.api.engine.SyncableType.BOOKMARKS
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SavedSitesSyncMergerTest {

    private val repository: SavedSitesRepository = mock()
    private val syncCrypto: SyncCrypto = mock()
    private lateinit var parser: SavedSitesSyncMerger

    @Before
    fun before() {
        parser = SavedSitesSyncMerger(repository, syncCrypto)

        whenever(syncCrypto.decrypt(ArgumentMatchers.anyString()))
            .thenAnswer { invocation -> invocation.getArgument(0) }
    }

    @Test
    fun whenMergingEmptyListOfChangesThenResultIsSuccess() {
        val result = parser.merge(SyncChanges.empty())

        assertTrue(result is SyncMergeResult.Success)
    }

    @Test
    fun whenMergingCorruptListOfChangesThenResultIsError() {
        val corruptedChanges = SyncChanges(BOOKMARKS, "corruptedJson")
        val result = parser.merge(corruptedChanges)

        assertTrue(result is SyncMergeResult.Error)
    }

    @Test
    fun whenMergingChangesInFirstSyncDataIsStoredSuccessfully() {
    }
}
