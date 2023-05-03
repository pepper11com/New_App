package com.example.new_app.screens.task.tasklist

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.new_app.R
import com.example.new_app.SharedViewModel
import com.example.new_app.common.sort.getDueDateAndTime
import com.example.new_app.model.Task
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableTaskListItem(
    task: Task,
    onClick: () -> Unit,
    viewModel: TaskListViewModel,
    onLongPress: () -> Unit,
    status: TaskStatus,
    isSelected: MutableState<Boolean>,
    onSelectedTasksChange: (Task, Boolean) -> Unit,
    onTaskSwipedBackToActive: (Task) -> Unit,
    isFlashing: Boolean = false,
    mainViewModel: SharedViewModel,
    mapsVisible: Boolean
) {
    //width of the swipeable item
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val swipeableState = remember(task.id) { SwipeableState(0) }
    val maxOffset = with(LocalDensity.current) { screenWidth.toPx() }
    val anchors = when (status) {
        TaskStatus.DELETED -> mapOf(0f to 0, maxOffset to 1)
        TaskStatus.COMPLETED -> mapOf(-maxOffset to -1, 0f to 0)
        TaskStatus.ACTIVE -> mapOf(-maxOffset to -1, 0f to 0, maxOffset to 1)
    }

    val offset by animateOffsetAsState(
        targetValue = Offset(swipeableState.offset.value, 0f),
    )

    LaunchedEffect(swipeableState.currentValue) {
        when (swipeableState.targetValue) {
            -1 -> {
                if (status != TaskStatus.COMPLETED) {
                    viewModel.onTaskSwipeDeleted(task)
                } else {
                    viewModel.onTaskSwipeActive(task)
                }
                swipeableState.snapTo(0)
            }

            1 -> {
                if (status != TaskStatus.DELETED) {
                    viewModel.onTaskSwipeCompleted(task)
                } else {
                    viewModel.onTaskSwipeActive(task)
                }
                swipeableState.snapTo(0)
            }
        }
        if (swipeableState.targetValue == 0 && (status == TaskStatus.DELETED || status == TaskStatus.COMPLETED)) {
            onTaskSwipedBackToActive(task)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.6f) },
                orientation = Orientation.Horizontal
            )
    ) {
        TaskListItem(
            task = task,
            onClick = onClick,
            onLongPress = onLongPress,
            offset = offset,
            isSelected = isSelected,
            onSelectedTasksChange = onSelectedTasksChange,
            isFlashing = isFlashing,
            mainViewModel = mainViewModel,
            mapVisible = mapsVisible
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskListItem(
    task: Task,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    offset: Offset = Offset.Zero,
    isSelected: MutableState<Boolean>,
    onSelectedTasksChange: (Task, Boolean) -> Unit,
    isFlashing: Boolean = false,
    mainViewModel: SharedViewModel,
    mapVisible: Boolean = true,
    mapHeight: Dp = 140.dp
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val flashState = rememberSaveable { mutableStateOf(isFlashing) }
    val taskIsSelected = mainViewModel.selectedTaskIds.collectAsState().value.contains(task.id)
    val taskColor = task.color?.let { Color(it) }

    val flashColor = animateColorAsState(
        targetValue = if (flashState.value) Color(0xFFCCCCCC) else Color(0xFF444444),
        animationSpec = tween(durationMillis = 5000)
    )
    val animatedMapHeight = animateDpAsState(
        targetValue = if (mapVisible) mapHeight else 0.dp,
        animationSpec = tween(durationMillis = 400)
    )
    LaunchedEffect(flashState.value) {
        if (flashState.value) {
            flashState.value = false
        }
    }
    SideEffect {
        flashState.value = isFlashing
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = task.imageUri ?: R.drawable.baseline_account_box_24)
            .apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                error(R.drawable.baseline_account_box_24)
            }).build()
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = flashColor.value
        ),
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    onLongPress()
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            25,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                },
            )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = getDueDateAndTime(task),
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                taskColor?.let {
                    Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(color = it)
                        .size(30.dp)
                }?.let {
                    Box(
                        modifier = it
                    )
                }
                if (TaskStatus.ACTIVE != task.status) {
                    Checkbox(
                        checked = taskIsSelected,
                        onCheckedChange = { isChecked ->
                            isSelected.value = isChecked
                            onSelectedTasksChange(task, isChecked)
                            mainViewModel.onTaskSelection(task.id, isChecked)
                        },
                        colors = CheckboxDefaults.colors(
                            checkmarkColor = Color(0xFFFF8C00),
                            disabledCheckedColor = MaterialTheme.colorScheme.background,
                            disabledIndeterminateColor = MaterialTheme.colorScheme.background,
                            uncheckedColor = MaterialTheme.colorScheme.background,
                            checkedColor = MaterialTheme.colorScheme.background,
                        )
                    )
                }
            }
            if (task.locationName != null && task.location != null) {
                val staticMapUrl = generateStaticMapUrl(task)
                StaticMap(
                    staticMapUrl = staticMapUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(animatedMapHeight.value)
                        .clip(RoundedCornerShape(8.dp)),
                )
                Text(
                    modifier = Modifier.padding(start = 14.dp, bottom = 12.dp),
                    text = task.locationName.toString(),
                    fontSize = 9.sp,
                    color = Color.White,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(animatedMapHeight.value)
                        .background(Color(0xFF666666), shape = RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
