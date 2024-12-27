package dev.kuchta.geostudy

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GameViewModel : ViewModel() {
    private val _currentQuery = MutableStateFlow<Country>(Country(-1,"","", "", 0.0,0.0))
    private val _selectedCountry = MutableStateFlow<Country>(Country(-1,"","", "", 0.0,0.0))
    private val _remainingCountries = MutableStateFlow<List<Country>>(emptyList())
    private val _selectedContinent = MutableStateFlow<String>("")
    private val _correct = MutableStateFlow<Int>(0);
    private val _incorrect = MutableStateFlow<Int>(0);
    private val _mistakeAcknowledge = MutableStateFlow<Boolean>(true);

    val selectedContinent: StateFlow<String> = _selectedContinent
    val remainingCountries: StateFlow<List<Country>> = _remainingCountries
    val remainingCountriesCount: StateFlow<Int> = _remainingCountries.map { it.size }.stateIn(
        viewModelScope, SharingStarted.Eagerly,  0) // THIS? STOOPID AS HELL JUST TO EXPOSE A DERIVED STATE
    val currentQuery: StateFlow<Country> = _currentQuery
    val selectedCountry: StateFlow<Country> = _selectedCountry
    val correct: StateFlow<Int> = _correct
    val incorrect: StateFlow<Int> = _incorrect
    val mistakeAcknowledge: StateFlow<Boolean> = _mistakeAcknowledge

    fun selectContinent(continent : String) {
        _selectedContinent.value = continent;
        val countryIndices = country_continents.mapIndexed { index, value ->
            if (value == continent) index else null }.filterNotNull()
        _remainingCountries.value = countryIndices.map { Country(it) }
        popRemaining()
    }
    fun selectCountryByCode(code : String) {
        val idx = country_codes.indexOfFirst { c -> code == c}
        if(idx == -1) {
            return
        }
        if(country_codes[idx] == _selectedCountry.value.isoCode) {
            deselectCurrent()
            return
        }
        _selectedCountry.value = Country(idx);
    }
    fun deselectCurrent() {
        _selectedCountry.value = Country(-1,"","", "", 0.0,0.0)
    }
    fun popRemaining() {
        _remainingCountries.value = _remainingCountries.value.filterNot { it.name== _currentQuery.value.name }
        _currentQuery.value = _remainingCountries.value.random()
    }
    fun acknowledgeMistake() {
        _mistakeAcknowledge.value = true
        popRemaining()
    }
    fun confirmCountry() {

        if(_selectedCountry.value.name == _currentQuery.value.name) {
            _correct.value += 1
            popRemaining()
        } else {
            _incorrect.value += 1
            _mistakeAcknowledge.value = false
        }
        deselectCurrent()
    }

}