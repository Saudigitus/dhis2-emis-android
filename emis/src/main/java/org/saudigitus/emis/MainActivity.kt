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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import org.dhis2.commons.Constants
import org.saudigitus.emis.ui.attendance.AttendanceScreen
import org.saudigitus.emis.ui.attendance.AttendanceViewModel
import org.saudigitus.emis.ui.home.HomeScreen
import org.saudigitus.emis.ui.home.HomeViewModel
import org.saudigitus.emis.ui.performance.PerformanceScreen
import org.saudigitus.emis.ui.performance.PerformanceViewModel
import org.saudigitus.emis.ui.subjects.SubjectScreen
import org.saudigitus.emis.ui.subjects.SubjectViewModel
import org.saudigitus.emis.ui.teis.TeiScreen
import org.saudigitus.emis.ui.theme.EMISAndroidTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EMISAndroidTheme(
                darkTheme = false,
                dynamicColor = false,
            ) {
                viewModel.setBundle(intent?.extras)

                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.HOME_ROUTE,
                    ) {
                        composable(AppRoutes.HOME_ROUTE) {
                            viewModel.setProgram(intent?.extras?.getString(Constants.PROGRAM_UID) ?: "")

                            HomeScreen(
                                viewModel = viewModel,
                                onBack = { finish() },
                                navTo = navController::navigate,
                            )
                        }
                        composable(AppRoutes.TEI_LIST_ROUTE) {
                            TeiScreen(
                                viewModel = viewModel,
                                onBack = navController::navigateUp,
                            )
                        }
                        composable(
                            route = "${AppRoutes.ATTENDANCE_ROUTE}/{ou}",
                            arguments = listOf(
                                navArgument("ou") {
                                    type = NavType.StringType
                                },
                            ),
                        ) {
                            val attendanceViewModel: AttendanceViewModel = hiltViewModel()

                            attendanceViewModel.setProgram(intent?.extras?.getString(Constants.PROGRAM_UID) ?: "")
                            attendanceViewModel.setTeis(viewModel.teis.collectAsStateWithLifecycle().value)
                            attendanceViewModel.setInfoCard(viewModel.infoCard.collectAsStateWithLifecycle().value)
                            attendanceViewModel.setOU(it.arguments?.getString("ou") ?: "")

                            AttendanceScreen(attendanceViewModel, navController::navigateUp)
                        }
                        composable(
                            route = "${AppRoutes.PERFORMANCE_ROUTE}/{ou}/{stage}/{dataElement}/{subjectName}",
                            arguments = listOf(
                                navArgument("ou") {
                                    type = NavType.StringType
                                },
                                navArgument("stage") {
                                    type = NavType.StringType
                                },
                                navArgument("dataElement") {
                                    type = NavType.StringType
                                },
                                navArgument("subjectName") {
                                    type = NavType.StringType
                                },
                            ),
                        ) {
                            val performanceViewModel = hiltViewModel<PerformanceViewModel>()
                            val uiState by performanceViewModel.uiState.collectAsStateWithLifecycle()
                            val infoCard by performanceViewModel.infoCard.collectAsStateWithLifecycle()
                            val stats by performanceViewModel.cache.collectAsStateWithLifecycle()
                            val teis by viewModel.teis.collectAsStateWithLifecycle()
                            val performanceStep by performanceViewModel.buttonStep.collectAsStateWithLifecycle()

                            val stage = it.arguments?.getString("stage") ?: ""
                            val dl = it.arguments?.getString("dataElement") ?: ""
                            val ou = it.arguments?.getString("ou") ?: ""

                            performanceViewModel.setOU(ou)
                            performanceViewModel.setProgram(intent?.extras?.getString(Constants.PROGRAM_UID) ?: "")
                            performanceViewModel.loadSubjects(stage)
                            performanceViewModel.setTeis(teis, performanceViewModel::updateTEISList)
                            performanceViewModel.setInfoCard(viewModel.infoCard.collectAsStateWithLifecycle().value)
                            performanceViewModel.getFields(stage, dl)
                            performanceViewModel.setDefault(stage, dl)

                            PerformanceScreen(
                                state = uiState,
                                onNavBack = navController::navigateUp,
                                infoCard = infoCard,
                                defaultSelection = it.arguments?.getString("subjectName") ?: "",
                                setPerformanceState = performanceViewModel::fieldState,
                                performanceStats = Pair("${stats.size}", "${teis.size.minus(stats.size)}"),
                                performanceStep = performanceStep,
                                setDate = performanceViewModel::setDate,
                                onNext = performanceViewModel::onClickNext,
                                step = performanceViewModel::setButtonStep,
                                onFilterClick = performanceViewModel::updateDataFields,
                                onSave = performanceViewModel::save,
                            )
                        }
                        composable(
                            route = "${AppRoutes.SUBJECT_ROUTE}/{ou}",
                            arguments = listOf(
                                navArgument("ou") {
                                    type = NavType.StringType
                                },
                            ),
                        ) {
                            val subjectViewModel = hiltViewModel<SubjectViewModel>()
                            val state by subjectViewModel.uiState.collectAsStateWithLifecycle()
                            val stage by subjectViewModel.programStage.collectAsStateWithLifecycle()
                            val infoCard by viewModel.infoCard.collectAsStateWithLifecycle()
                            subjectViewModel.setProgram(intent?.extras?.getString(Constants.PROGRAM_UID) ?: "")
                            val ou = it.arguments?.getString("ou") ?: ""

                            SubjectScreen(
                                state = state,
                                onBack = navController::navigateUp,
                                onFilterClick = subjectViewModel::performOnFilterClick,
                                infoCard = infoCard,
                                onClick = { subjectId, subjectName ->
                                    navController.navigate("${AppRoutes.PERFORMANCE_ROUTE}/$ou/$stage/$subjectId/$subjectName")
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
