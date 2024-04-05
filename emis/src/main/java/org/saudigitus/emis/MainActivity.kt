package org.saudigitus.emis

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.dhis2.commons.Constants
import org.saudigitus.emis.ui.attendance.AttendanceScreen
import org.saudigitus.emis.ui.attendance.AttendanceViewModel
import org.saudigitus.emis.ui.home.HomeScreen
import org.saudigitus.emis.ui.marks.MarksScreen
import org.saudigitus.emis.ui.marks.MarksViewModel
import org.saudigitus.emis.ui.subjects.SubjectScreen
import org.saudigitus.emis.ui.subjects.SubjectViewModel
import org.saudigitus.emis.ui.teis.TeiScreen
import org.saudigitus.emis.ui.home.HomeViewModel
import org.saudigitus.emis.ui.theme.EMISAndroidTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EMISAndroidTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                viewModel.setBundle(intent?.extras)

                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.HOME_ROUTE
                    ) {
                        composable(AppRoutes.HOME_ROUTE) {
                            HomeScreen(
                                viewModel = viewModel,
                                onBack = { finish() },
                            ) {
                                navController.navigate(it)
                            }
                        }
                        composable(AppRoutes.TEI_LIST_ROUTE) {
                            TeiScreen(
                                viewModel = viewModel,
                                onBack = { finish() },
                            ) {
                                navController.navigate(AppRoutes.SUBJECT_ROUTE)
                            }
                        }
                        composable(AppRoutes.ATTENDANCE_ROUTE) {
                            val attendanceViewModel: AttendanceViewModel = hiltViewModel()

                            attendanceViewModel.setProgram(intent?.extras?.getString(Constants.PROGRAM_UID) ?: "")
                            attendanceViewModel.setTeis(viewModel.teis.collectAsStateWithLifecycle().value)
                            attendanceViewModel.setInfoCard(viewModel.infoCard.collectAsStateWithLifecycle().value)

                            AttendanceScreen(attendanceViewModel) {
                                navController.navigateUp()
                            }
                        }
                        composable(AppRoutes.PERFORMANCE_ROUTE) {
                            val marksViewModel = hiltViewModel<MarksViewModel>()
                            val uiState by marksViewModel.uiState.collectAsStateWithLifecycle()
                            val infoCard by marksViewModel.infoCard.collectAsStateWithLifecycle()

                            marksViewModel.setProgram(intent?.extras?.getString(Constants.PROGRAM_UID) ?: "")
                            marksViewModel.setTeis(
                                viewModel.teis.collectAsStateWithLifecycle().value,
                                marksViewModel::updateTEISList
                            )
                            marksViewModel.setInfoCard(viewModel.infoCard.collectAsStateWithLifecycle().value)

                            MarksScreen(
                                state = uiState,
                                onNavBack = navController::navigateUp,
                                infoCard = infoCard,
                                setMarksState = marksViewModel::marksState,
                                setDate = marksViewModel::setDate,
                                onNext = marksViewModel::onClickNext,
                                onSave = marksViewModel::save
                            )
                        }
                        composable(AppRoutes.SUBJECT_ROUTE) {
                            val subjectViewModel = hiltViewModel<SubjectViewModel>()
                            val state by subjectViewModel.uiState.collectAsStateWithLifecycle()
                            subjectViewModel.setConfig(intent?.extras?.getString(Constants.PROGRAM_UID) ?: "")

                            SubjectScreen(
                                state = state,
                                onBack = navController::navigateUp,
                                onFilterClick = subjectViewModel::performOnFilterClick,
                                onClick = subjectViewModel::performOnClick
                            )
                        }
                    }
                }
            }
        }
    }
}
