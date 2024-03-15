package org.saudigitus.emis.ui.marks

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.local.repository.FormRepositoryImpl
import org.saudigitus.emis.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class MarksViewModel
@Inject constructor(
    repository: DataManager,
    private val formRepositoryImpl: FormRepositoryImpl
): BaseViewModel(repository) {


    override fun setConfig(program: String) {
        TODO("Not yet implemented")
    }

    override fun setProgram(program: String) {
        TODO("Not yet implemented")
    }

    override fun setTeis(teis: List<SearchTeiModel>) {
        TODO("Not yet implemented")
    }

    override fun setDate(date: String) {
        TODO("Not yet implemented")
    }

}