/*
 * Copyright 2018, The Android Open Source Project
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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.databinding.adapters.SeekBarBindingAdapter
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        private var tonight = MutableLiveData<SleepNight?>()
        private val viewModelJob=Job()
        private val nights=database.getAllNights()

        private val _navigateToSleepQulity=MutableLiveData<SleepNight>()
        val navigateToSleepQulity:LiveData<SleepNight> get() = _navigateToSleepQulity

        val startButtonVisible = Transformations.map(tonight) {
                null == it
        }
        val stopButtonVisible = Transformations.map(tonight) {
                null != it
        }
        val clearButtonVisible = Transformations.map(nights) {
                it?.isNotEmpty()
        }

        private val _showSnapBarEvent=MutableLiveData<Boolean>()
        val showSnapBarEvent:LiveData<Boolean> get() = _showSnapBarEvent

        fun doneShowingSnapBar(){
                _showSnapBarEvent.value=false
        }


        fun doneNavigating()
        {
                _navigateToSleepQulity.value=null
        }

        override fun onCleared() {
                super.onCleared()
                viewModelJob.cancel()
        }

        private val uiScope= CoroutineScope(Dispatchers.Main+viewModelJob)
        init {
            intilaizeTonight()
        }

        private fun intilaizeTonight() {
                uiScope.launch {
                        tonight.value=getToNightFromDataBase()
                }
        }

        private suspend fun getToNightFromDataBase(): SleepNight? {
                return withContext(Dispatchers.IO)
                {
                        var night=database.getTonight()
                        if (night?.endTimeMilli !=night?.startTimeMilli){
                                night=null}
                        night
                }
        }
        fun onStartTraking()
        {
                uiScope.launch {
                        val newNight=SleepNight()
                        insert(newNight)
                                tonight.value=getToNightFromDataBase()
                }
        }
        private suspend fun insert(night: SleepNight)
        {
                withContext(Dispatchers.IO)
                {
                        database.insert(night)
                }
        }
        fun someWorkNeedToBeDone()
        {
                uiScope.launch {
                        suspendFunction()
                }
        }

        suspend fun suspendFunction() {
                withContext(Dispatchers.IO){

                }
        }
        fun onStopTracking(){
                uiScope.launch{
                        var oldNight=tonight.value?:return@launch
                        oldNight.endTimeMilli=System.currentTimeMillis()
                update(oldNight)
                        _navigateToSleepQulity.value=oldNight

                }

        }
        private suspend fun update(night: SleepNight)
        {
                withContext(Dispatchers.IO)
                {
                        database.update(night)
                }
        }
         fun onClear()
        {
                uiScope.launch {
                        clear()
                        tonight.value=null
                 _showSnapBarEvent.value=true
                }
        }
     private    suspend fun clear()
        {
                withContext(Dispatchers.IO){
                                database.clear()
                }

        }

}

