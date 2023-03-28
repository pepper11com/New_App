package com.example.new_app.screens.createtask

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.new_app.R
import com.example.new_app.common.composables.CustomButton
import com.example.new_app.common.composables.CustomTextField
import com.example.new_app.common.composables.RegularCardEditor
import com.example.new_app.common.usable.saveImageUriPermission
import com.example.new_app.model.Task
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.lang.Integer.min


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateTaskScreen(
    popUpScreen: () -> Unit,
    taskId: String,
    saveImageUriPermission: (Uri) -> Unit,
    userId: String
) {
    val viewModel: CreateTaskViewModel = viewModel()
    val task by viewModel.task


    LaunchedEffect(Unit) {
        viewModel.initialize(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Task") },
                navigationIcon = {
                    IconButton(onClick = popUpScreen) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            PickImageFromGallery(
                LocalContext.current,
                viewModel,
                task,
                saveImageUriPermission,
                userId
            )

            CustomTextField(
                value = task.title,
                onValueChange = viewModel::onTitleChange,
                label = "Title",
                modifier = Modifier.fillMaxWidth()
            )

            CustomTextField(
                value = task.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Description",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )

            CardEditors(task, viewModel::onDateChange, viewModel::onTimeChange)


            CustomButton(
                onClick = {
                    viewModel.onDoneClick(popUpScreen)
                },
                text = "Create Task",
                modifier = Modifier.fillMaxWidth(),
                enabled = task.title.isNotBlank() && task.description.isNotBlank()
            )

        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun CardEditors(
    task: Task,
    onDateChange: (Long) -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    val activity = LocalContext.current as AppCompatActivity

    RegularCardEditor(
        R.string.date,
        Icons.Filled.DateRange,
        task.dueDate,
        Modifier.padding(top = 16.dp)
    ) {
        showDatePicker(activity, onDateChange)
    }

    RegularCardEditor(
        R.string.time,
        Icons.Filled.Timer,
        task.dueTime,
        Modifier.padding(top = 16.dp)
    ) {
        showTimePicker(activity, onTimeChange)
    }
}


@Composable
fun PickImageFromGallery(
    context: Context,
    viewModel: CreateTaskViewModel,
    task: Task,
    saveImageUriPermission: (Uri) -> Unit,
    userId: String
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.onImageChange(uri.toString(), context, task.id, userId)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } else {
                    val clipData = result.data?.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val item = clipData.getItemAt(i)
                            val uri = item.uri
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            )
                        }
                    }
                }
                saveImageUriPermission(uri)
            }
        }
    }

    fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        }
        launcher.launch(intent)
    }

    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (task.imageUri != null && task.imageUri!!.isNotEmpty()) {
            LaunchedEffect(task.imageUri) {
                viewModel.bitmap = if (task.imageUri != null && task.imageUri!!.isNotEmpty()) {
                    BitmapFactory.decodeFile(task.imageUri)
                } else {
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.baseline_account_box_24
                    )
                }
            }


            viewModel.bitmap?.let { btm ->
                val squareBitmap = btm.centerCropToSquare()
                val softwareBitmap = squareBitmap.toSoftwareBitmap()
                val circularBitmap = softwareBitmap.toCircularBitmap()
                Image(
                    bitmap = circularBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .weight(1f, fill = false)
                )
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.baseline_account_box_24),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f, fill = false)
        ) {
            Button(
                onClick = {
                    openImagePicker()
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colors.onPrimary,
                    backgroundColor = MaterialTheme.colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Pick Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                enabled = task.imageUri != null && task.imageUri!!.isNotEmpty(),
                onClick = {
                    viewModel.onImageChange("", context, task.id, userId)
                    task.imageUri = null
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colors.onPrimary,
                    backgroundColor = MaterialTheme.colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Delete Image")
            }
        }
    }
}


// Extension function to crop a Bitmap into a square
fun Bitmap.centerCropToSquare(): Bitmap {
    val dimension = min(width, height)
    val xOffset = (width - dimension) / 2
    val yOffset = (height - dimension) / 2
    return Bitmap.createBitmap(this, xOffset, yOffset, dimension, dimension)
}

// Extension function to crop a Bitmap into a circle
fun Bitmap.toCircularBitmap(): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
    }
    val rect = Rect(0, 0, width, height)
    val rectF = RectF(rect)

    canvas.drawOval(rectF, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)

    return output
}

// Extension function to convert a hardware Bitmap to a software Bitmap
fun Bitmap.toSoftwareBitmap(): Bitmap {
    val config = if (isMutable) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    return copy(config, isMutable)
}


private fun showDatePicker(activity: AppCompatActivity, onDateChange: (Long) -> Unit) {
    val picker = MaterialDatePicker.Builder.datePicker().build()

    activity.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { timeInMillis -> onDateChange(timeInMillis) }
    }
}

private fun showTimePicker(activity: AppCompatActivity, onTimeChange: (Int, Int) -> Unit) {
    val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build()

    activity.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { onTimeChange(picker.hour, picker.minute) }
    }
}



