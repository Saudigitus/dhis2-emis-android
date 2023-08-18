package org.saudigitus.emis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.OU

data class Item(
    val id: String,
    val itemName: String,
    val code: String? = null
)

@Composable
fun DropDownOu(
    modifier: Modifier = Modifier,
    placeholder: String,
    leadingIcon: ImageVector,
    selectedSchool: OU? = null,
    program: String,
    onItemClick: (OU) -> Unit
) {
    val context = LocalContext.current
    val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager

    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value) {
        launchOuTreeSelector(
            supportFragmentManager = fragmentManager!!,
            selectedSchool = selectedSchool,
            program = program,
            onSchoolSelected = {
                onItemClick.invoke(it)
            }
        )
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(30.dp),
                    clip = false
                )
                .background(color = Color.White, shape = RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp),
            value = selectedSchool?.displayName ?: "",
            onValueChange = {},
            singleLine = true,
            readOnly = true,
            placeholder = { Text(text = placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color(0xFF2C98F0)
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    launchOuTreeSelector(
                        supportFragmentManager = fragmentManager!!,
                        selectedSchool = selectedSchool,
                        program = program,
                        onSchoolSelected = {
                            onItemClick.invoke(it)
                        }
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
            )
        )
    }
}

fun launchOuTreeSelector(
    supportFragmentManager: FragmentManager,
    selectedSchool: OU? = null,
    program: String,
    onSchoolSelected: (school: OU) -> Unit
) {
    OUTreeFragment.Builder()
        .showAsDialog()
        .singleSelection()
        .orgUnitScope(OrgUnitSelectorScope.ProgramCaptureScope(program))
        .withPreselectedOrgUnits(
            selectedSchool?.let { listOf(it.uid) } ?: emptyList()
        )
        .onSelection { selectedOrgUnits ->
            val selectedOrgUnit = selectedOrgUnits.firstOrNull()
            if (selectedOrgUnit != null) {
                onSchoolSelected(
                    OU(
                        uid = selectedOrgUnit.uid(),
                        displayName = selectedOrgUnit.displayName()
                    )
                )
            }
        }
        .build()
        .show(supportFragmentManager, "OU_TREE")
}

@Composable
fun DropDown(
    modifier: Modifier = Modifier,
    placeholder: String,
    leadingIcon: ImageVector,
    data: List<Item>?,
    selectedItemName: String = "",
    onItemClick: (Item) -> Unit
) {
    var selectedItemIndex by remember { mutableStateOf(-1) }
    var selectedItem by remember { mutableStateOf(selectedItemName) }
    var expand by remember { mutableStateOf(false) }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    var onClearSelection by remember { mutableStateOf(false) }

    if (selectedItemIndex > 0 && onClearSelection) {
        selectedItemIndex = 0
        onClearSelection = false
        selectedItem = data?.get(selectedItemIndex)!!.itemName
    }

    val paddingValue = if (selectedItemIndex >= 0) {
        4.dp
    } else {
        0.dp
    }

    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value) {
        expand = !expand
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .shadow(
                    elevation = 2.dp,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(30.dp),
                    clip = false
                )
                .background(color = Color.White, shape = RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp),
            value = selectedItem,
            onValueChange = {
                selectedItem = it
            },
            singleLine = true,
            readOnly = true,
            placeholder = { Text(text = placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color(0xFF2C98F0)
                )
            },
            trailingIcon = {
                IconButton(onClick = { expand = !expand }) {

                    Icon(
                        imageVector = if (!expand)
                            Icons.Default.ArrowDropDown
                        else
                            Icons.Default.ArrowDropUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
            )
        )

        DropdownMenu(
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                .background(color = Color.White),
            offset = DpOffset(0.dp, 2.dp),
            expanded = expand,
            onDismissRequest = {
                expand = !expand
            }
        ) {
            data?.forEachIndexed { index, item ->
                Row(Modifier.padding(horizontal = 10.dp)) {
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (selectedItemIndex == index)
                                    Color.LightGray.copy(.5f)
                                else
                                    Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(paddingValue),
                        text = {
                            Text(
                                text = item.itemName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = true,
                                style = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily(Font(R.font.rubik_regular))
                                )
                            )
                        },
                        onClick = {
                            onItemClick(item)
                            expand = !expand
                            selectedItem = item.itemName
                            selectedItemIndex = index
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = item.itemName,
                                tint = Color(0xFF2C98F0)
                            )
                        }
                    )
                }
            }
        }
    }
}
