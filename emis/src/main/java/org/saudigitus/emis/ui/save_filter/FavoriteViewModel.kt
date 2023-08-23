package org.saudigitus.emis.ui.save_filter

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
import timber.log.Timber
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

    fun setFavorite(
        schoolUid: String = "",
        school: String? = null,
        gradeCode: String? = null,
        sectionCode: String? = null,
        sectionName: String? =  null,
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
            junkSections.add(Section(sectionCode, sectionName))
            _sections.value = junkSections

            _stream.update {
                it.copy(sections = sections.value)
            }

            val record = favorites.value.find { it.uid == favorite.value.uid }

            record?.stream?.let { junkStream.addAll(it) }
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


    fun save() {
        viewModelScope.launch {
            val favoritesCache = mutableListOf<Favorite>()
            favoritesCache.addAll(favorites.value)
            favoritesCache.add(favorite.value)

            repository.save(FavoriteConfig(favoritesCache))

            Timber.tag("FAVORITE_savE").e("${favorite.value}")
        }
    }

    private fun getFavorites() {
        viewModelScope.launch {
            repository.getFavorites().collectLatest {
                _favorites.value = it.favorites ?: emptyList()
            }
        }
    }

}