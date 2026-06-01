package com.example.timereminder.ui.screen.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timereminder.data.repository.TagRepository
import com.example.timereminder.domain.model.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TagManageViewModel(
    private val tagRepository: TagRepository
) : ViewModel() {

    val tags: StateFlow<List<Tag>> = tagRepository.allTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showDeleteDialog = MutableStateFlow<Tag?>(null)
    val showDeleteDialog: StateFlow<Tag?> = _showDeleteDialog.asStateFlow()

    fun addTag(name: String, color: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            tagRepository.saveTag(Tag(name = name, color = color))
        }
    }

    fun updateTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.saveTag(tag)
        }
    }

    fun confirmDeleteTag(tag: Tag) {
        _showDeleteDialog.value = tag
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagRepository.deleteTag(tag)
            _showDeleteDialog.value = null
        }
    }

    class Factory(
        private val tagRepository: TagRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TagManageViewModel(tagRepository) as T
        }
    }
}
