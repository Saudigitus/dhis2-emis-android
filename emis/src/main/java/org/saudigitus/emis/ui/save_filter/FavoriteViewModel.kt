package org.saudigitus.emis.ui.save_filter

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.saudigitus.emis.data.local.FavoriteConfigRepository
import org.saudigitus.emis.data.model.Favorite
import org.saudigitus.emis.data.model.FavoriteConfig
import org.saudigitus.emis.data.model.Section
import org.saudigitus.emis.data.model.Stream
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel
@Inject constructor(
    val repository: FavoriteConfigRepository
): ViewModel() {

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites

    private val _favorite = MutableStateFlow(Favorite())
    val favorite: StateFlow<Favorite> = _favorite

    private val _stream = MutableStateFlow(Stream())
    private val stream: StateFlow<Stream> = _stream

    private val _sections = MutableStateFlow<List<Section>>(emptyList())
    private val sections: StateFlow<List<Section>> = _sections

    //private val junkSections = mutableListOf<Section>()
    //private val junkStream = mutableListOf<Stream>()

    fun removeFavorite(favorite: Favorite) {
        val updatedList = _favorites.value.toMutableList()
        updatedList.remove(favorite)
        _favorites.value = updatedList
    }

    init {
        getFavorites()
    }

    fun removeItem(sectionCode: String){
        val updatedFavorites = _favorite.value.stream
        val sectionsValues = updatedFavorites.flatMap { it.sections }.toMutableList()

        sectionsValues.removeIf { it.code == sectionCode }
        println("SV: $sectionsValues")

        _sections.value  = sectionsValues
    }

    fun setFavorite(
        schoolUid: String = "",
        school: String? = null,
        gradeCode: String? = null,
        sectionCode: String? = null,
        sectionName: String? =  null,
        isSelected: Boolean  = false
    ){
        if(school != null) {
            _favorite.update {
                it.copy(uid = schoolUid, school = school)
            }
        }

        if(sectionCode != null) {
            val junkSections = mutableListOf<Section>()
            val junkStream = mutableListOf<Stream>()

            junkSections.addAll(sections.value)

            if(isSelected){
                junkSections.add(Section(sectionCode, sectionName))
            } else {
                val objectToRemove = junkSections.find { it.code == sectionCode }
                objectToRemove?.let {
                    junkSections.remove(it)
                }
            }

            _sections.value = junkSections

            _stream.update {
                it.copy(sections = sections.value)
            }

            junkStream.add(stream.value)

            _favorite.update {
                it.copy(stream = junkStream)
            }
        }

        if(gradeCode != null) {
            _stream.update {
                it.copy(grade = gradeCode)
            }
        }
    }

    fun reset(){
        viewModelScope.launch {
            repository.save(FavoriteConfig(emptyList()))
        }
    }

    fun clear() {
        //junkSections.clear()
        //junkStream.clear()

        _favorite.value = Favorite()
        _stream.value = Stream()
        _sections.value = emptyList()
    }

    fun save() {
        viewModelScope.launch {
            val favoritesCache = mutableListOf<Favorite>()
            favoritesCache.addAll(favorites.value)
            favoritesCache.add(favorite.value)

            repository.save(FavoriteConfig(favoritesCache))

            clear()
        }
    }

    fun showToast(context: Context, message: String){
        Toast.makeText(context, "$message", Toast.LENGTH_LONG).show()
    }

    private fun getFavorites() {
        viewModelScope.launch {
            repository.getFavorites().collectLatest {
                _favorites.value = it.favorites ?: emptyList()
            }
        }
    }

}