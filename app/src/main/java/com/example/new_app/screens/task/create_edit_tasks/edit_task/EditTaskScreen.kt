package com.example.new_app.screens.task.create_edit_tasks.edit_task

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.new_app.SharedViewModel
import com.example.new_app.TASK_MAP_SCREEN
import com.example.new_app.common.composables.CardEditors
import com.example.new_app.common.composables.ColorPicker
import com.example.new_app.common.composables.CustomEditTaskAppBar
import com.example.new_app.common.composables.CustomMultiLineTextfield
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.LoadingIndicator
import com.example.new_app.common.composables.PickImageFromGallery
import com.example.new_app.common.composables.SectionTitle
import com.example.new_app.common.composables.ShowLocation
import com.example.new_app.common.composables.customTextFieldColors
import com.example.new_app.common.util.Resource
import com.example.new_app.screens.task.create_edit_tasks.TaskEditCreateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun EditTaskScreen(
    popUpScreen: () -> Unit,
    taskId: String,
    userId: String,
    mainViewModel: SharedViewModel,
    openScreen: (String) -> Unit,
    viewModel: TaskEditCreateViewModel
) {

    val task by viewModel.task
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val newTaskState by viewModel.taskEditCreateState.collectAsState()

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.onImageChange(it)
            }
        }
    val init = mainViewModel.initEdit.value
    LaunchedEffect(Unit) {
        if (init == true){
            Log.d("EditTaskScreen", "LaunchedEffect: INIT")
            viewModel.initialize(taskId)
            mainViewModel.toggleInitEdit()
        }
    }

    // Task(id=6y04BhDo0yJaK9EwlSdz, title=kladdddd, description=klad,
    // createdBy=ZnqD1qmsKhZoIvR5m3GjpoSviEG3, dueDate=Tue, 2 May 2023, dueTime=16:52, assignedTo=[ZnqD1qmsKhZoIvR5m3GjpoSviEG3],
    // isCompleted=false, status=ACTIVE, taskDate=Tue May 02 15:46:12 GMT+02:00 2023, color=-9675909, alertMessageTimer=3600000,
    // imageUri=null, location=CustomLatLng(latitude=56.284643, longitude=9.435532), locationName=8620 Kjellerup, Denemarken)

    Scaffold(
        topBar = {
            CustomEditTaskAppBar(
                popUpScreen = popUpScreen,
                task = task,
                viewModel = viewModel,
                taskId = taskId,
                mainViewModel = mainViewModel,
                context = context,
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                SectionTitle("Image")
                PickImageFromGallery(
                    LocalContext.current,
                    viewModel,
                    task,
                    userId,
                    galleryLauncher
                )
            }

            item {
                Divider()
            }

            item {
                SectionTitle("Color")
                task.color?.let {
                    ColorPicker(
                        it,
                        viewModel::onColorChange
                    )
                }
            }

            item {
                Divider()
            }

            item {
                SectionTitle("Title & Description")
                CustomTextField(
                    value = task.title,
                    onValueChange = viewModel::onTitleChange,
                    label = "Title",
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )
                CustomMultiLineTextfield(
                    value = task.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    hintText = "Description",
                    textStyle = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    colors = customTextFieldColors()
                )
            }

            item {
                Divider(
                    modifier = Modifier
                        .padding(top = 18.dp)
                )
            }

            item {
                // Date Time Location Notification
                SectionTitle("Location, Date, Time & Notification")
                ShowLocation(
                    locationDisplay = viewModel.locationDisplay,
                    onEditClick = { openScreen(TASK_MAP_SCREEN) },
                    onLocationReset = viewModel::onLocationReset,
                )

                CardEditors(
                    task,
                    viewModel::onDateChange,
                    viewModel::onTimeChange,
                    viewModel = viewModel
                )
            }
        }

        when (newTaskState) {
            is Resource.Loading -> {
                // Display a loading indicator
                LoadingIndicator()
            }
            is Resource.Success -> {

            }

            is Resource.Error -> {
                // Handle error
            }
            else -> {
                // Handle empty state
            }
        }
    }
}



