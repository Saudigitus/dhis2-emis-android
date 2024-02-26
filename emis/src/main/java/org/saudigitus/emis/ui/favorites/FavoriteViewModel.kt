package org.saudigitus.emis.ui.favorites

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        getFavorites()
    }
    private fun removeFavorite(favorite: Favorite) {
        val updatedList = _favorites.value.toMutableList()
        updatedList.remove(favorite)
        _favorites.value = updatedList
    }

    fun setFavorite(
        schoolUid: String = "",
        school: String? = null,
        gradeCode: String? = null,
        gradeName: String? = null,
        sectionCode: String? = null,
        sectionName: String? =  null,
        isSelected: Boolean  = false
    ){
        if(school != null) {
            _favorite.update {
                it.copy(uid = schoolUid, school = school)
            }
        }

        val existingFavorite = favorites.value.find { it.uid == _favorite.value.uid }
        if (existingFavorite != null) {
            if(gradeCode != null) {
                _stream.update {
                    it.copy(grade = gradeName, code = gradeCode)
                }
            }

            val updatedFavorite = existingFavorite.copy(
                stream = existingFavorite.stream.toMutableList().apply {
                    if (sectionCode != null && isSelected) {
                        val junkSections = mutableListOf<Section>()
                        junkSections.addAll(sections.value)
                        junkSections.add(Section(sectionCode, sectionName))
                        _sections.value = junkSections
                        add(Stream(grade = _stream.value.grade, code =_stream.value.code, sections = sections.value))
                    }
                }
            )

            _favorite.value = updatedFavorite
        } else {
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
                    it.copy(grade = gradeName, code = gradeCode)
                }
            }
        }
    }

    fun reset(){
        viewModelScope.launch {
            repository.save(FavoriteConfig(emptyList()))
        }
    }

    fun clear() {
        _favorite.value = Favorite()
        _stream.value = Stream()
        _sections.value = emptyList()
    }

    fun save() {

        val existingFavorite = favorites.value.find { it.uid == _favorite.value.uid }
        if (existingFavorite != null) {
            removeFavorite(existingFavorite)
        }
        viewModelScope.launch {
            val favoritesCache = mutableListOf<Favorite>()
            favoritesCache.addAll(favorites.value)
            favoritesCache.add(favorite.value)
            repository.save(FavoriteConfig(favoritesCache))
            clear()
        }
    }

    fun showToast(context: Context, message: String){
        /**  TODO("Remove sigle expressions") */
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