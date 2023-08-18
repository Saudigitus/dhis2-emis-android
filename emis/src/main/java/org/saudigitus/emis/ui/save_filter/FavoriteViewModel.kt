package org.saudigitus.emis.ui.save_filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.saudigitus.emis.data.local.FavoriteConfigRepository
import org.saudigitus.emis.data.model.Favorite
import org.saudigitus.emis.data.model.FavoriteConfig
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel
@Inject constructor(
    val repository: FavoriteConfigRepository
): ViewModel() {

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites

    init {
        getFavorites()
    }

    fun save(favorites: List<Favorite>) {
        viewModelScope.launch {
            repository.save(FavoriteConfig(favorites))
        }
    }

    fun getFavorites() {
        viewModelScope.launch {
            repository.getFavorites().collect {
                _favorites.value = it.favorites ?: emptyList()
            }
        }
    }

}