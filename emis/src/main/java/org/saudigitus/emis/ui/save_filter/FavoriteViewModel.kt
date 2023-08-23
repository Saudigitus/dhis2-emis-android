package org.saudigitus.emis.ui.save_filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _streams = MutableStateFlow<List<Stream>>(emptyList())
    val streams: StateFlow<List<Stream>> = _streams

    private val _sections = MutableStateFlow<List<Section>>(emptyList())
    val sections: StateFlow<List<Section>> = _sections


    init {
        getFavorites()
    }

    fun setFavorite(
        school: String? = null,
        gradeCode: String? = null,
        sectionCode: String? = null,
        sectionName: String? =  null,
    ){
        if(school != null) {
            _favorite.update {
                it.copy(school = school)
            }
        }

        val junkSections = mutableListOf<Section>()
        if(sectionCode != null) {
            junkSections.addAll(sections.value)
            junkSections.add(Section(sectionCode, sectionName))
            _sections.value = junkSections

            println("SECTIONN J $junkSections")
            println("SECTIONN O ${sections.value}")
        }

        val junkStream = mutableListOf<Stream>()
        if(gradeCode != null) {
            junkStream.addAll(streams.value)
            junkStream.add(Stream(gradeCode, junkSections))
            _streams.value = junkStream

            println("STREAM OK ${streams.value}")
            println("STREAM OHK $junkStream")
            println("SECTIONN O2 ${sections.value}")

            _favorite.update {
                it.copy(stream = streams.value)
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
            repository.getFavorites().collect {
                _favorites.value = it.favorites ?: emptyList()
            }
        }
    }

}