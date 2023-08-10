package org.saudigitus.emis.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.MetadataItem
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {

    val students by viewModel.teis.collectAsStateWithLifecycle()
    val attendanceOptions by viewModel.attendanceOptions.collectAsStateWithLifecycle()
    val attendanceBtnState by viewModel.attendanceBtnState.collectAsStateWithLifecycle()
    val attendanceStep by viewModel.attendanceStep.collectAsStateWithLifecycle()
    val attendanceStatus by viewModel.attendanceStatus.collectAsStateWithLifecycle()
    val toolbarHeaders by viewModel.toolbarHeaders.collectAsStateWithLifecycle()
    val infoCard by viewModel.infoCard.collectAsStateWithLifecycle()


    Scaffold(
        topBar = {
            Toolbar(
                headers = toolbarHeaders,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C98F0),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationAction = { onBack.invoke() },
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = false,
                    filterVisibility = false,
                    showCalendar = true
                ),
                calendarAction = {
                    viewModel.setAttendanceDate(it)
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                   Text(
                       text = stringResource(R.string.update),
                       color = Color(0xFF2C98F0),
                       style = LocalTextStyle.current.copy(
                           fontFamily = FontFamily(Font(R.font.rubik_medium))
                       )
                   )
                },
                icon = {
                   Icon(
                       imageVector = Icons.Default.Edit,
                       contentDescription = null,
                       tint = Color(0xFF2C98F0)
                   )
                },
                onClick = { /*TODO*/ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFF2C98F0))
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.White,
                        shape = MaterialTheme.shapes.medium
                            .copy(
                                topStart = CornerSize(16.dp),
                                topEnd = CornerSize(16.dp),
                                bottomStart = CornerSize(0.dp),
                                bottomEnd = CornerSize(0.dp)
                            )
                    ),
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                ShowCard(infoCard)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(students) { student ->
                        MetadataItem(
                            displayName = "${
                                student.attributeValues?.values?.toList()?.get(2)?.value()
                            } ${student.attributeValues?.values?.toList()?.get(1)?.value()}",
                            attrValue = "${
                                student.attributeValues?.values?.toList()?.get(0)?.value()
                            }",
                            onClick = {}
                        ) {
                            if (attendanceStep == ButtonStep.EDITING) {
                                AttendanceItemState(
                                    tei = student.tei.uid(),
                                    attendanceState = attendanceStatus
                                )
                            } else {
                                AttendanceButtons(
                                    tei = student.tei.uid(),
                                    btnState = attendanceBtnState,
                                    actions = attendanceOptions
                                ) { index, tei, attendance ->
                                    Timber.tag("TEI_ATTENDANCE").e(attendance)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}