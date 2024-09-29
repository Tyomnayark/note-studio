package com.tyom.feature_library.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.tyom.core_ui.R
import com.tyom.core_ui.enums.FontEnum
import com.tyom.core_ui.extensions.FigmaLargePreview
import com.tyom.core_ui.extensions.IfTrue
import com.tyom.core_ui.extensions.noRippleClickable
import com.tyom.core_ui.models.PianoConfiguration
import com.tyom.core_ui.theme.PianoGray
import com.tyom.core_ui.theme.Red
import com.tyom.core_ui.theme.handjetRegularTextStyle
import com.tyom.core_ui.theme.ralewayExtraLightItalicTextStyle
import com.tyom.core_ui.theme.ralewayMediumTextStyle
import com.tyom.core_ui.theme.ralewayThinTextStyle
import com.tyom.core_ui.widgets.DisappearingAnimateContainer
import com.tyom.core_utils.extensions.empty
import com.tyom.core_utils.extensions.pxToDp
import com.tyom.domain.models.MusicalComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val BOTTOM_SHEET_ANIM_MILLIS = 300
private const val SHEET_HEIGHT_COEFF = 0.9f
private const val WIDTH_COEFF = 0.8f
private const val BUTTON_HEIGHT_COEFF = 0.333f
private const val BUTTON_SECOND_HEIGHT_COEFF = 0.5f
private const val DELETING_DELAY = 800L

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryPage(
    isModalBottomSheetIsOpened: Boolean,
    pianoConfiguration: PianoConfiguration,
    compositions: List<MusicalComposition>,
    fontStyle: FontEnum,

    testSave: () -> Unit,
    notifyBottomSheetWasClosed: () -> Unit,
    onClickExportJpeg: (title: String) -> Unit,
    onClickExportPdf: (title: String) -> Unit,
    onClickSaveComposition: (MusicalComposition) -> Unit,
    onClickDeleteComposition: (MusicalComposition) -> Unit,
    clickToChangeFontStyle: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true,
        animationSpec = tween(durationMillis = BOTTOM_SHEET_ANIM_MILLIS, easing = EaseOutCubic)
    )
    val height = dimensionResource(R.dimen._110dp)
    val padding = dimensionResource(R.dimen._10dp)
    val lazyListState = rememberLazyListState()
    val scrollStateModalSheet = rememberScrollState()
    val fontSize = with(LocalDensity.current) { dimensionResource(id = R.dimen._16sp).toSp() }
    val fontSizeTitle = with(LocalDensity.current) { dimensionResource(id = R.dimen._35sp).toSp() }
    var titleText by remember { mutableStateOf(TextFieldValue(String.empty())) }
    var isTextFocused by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val blockWidth =
        LocalConfiguration.current.screenWidthDp - dimensionResource(R.dimen._20dp).value
    val shapeCornerRad = dimensionResource(R.dimen._10dp)
    var isAnimationStarted by remember { mutableStateOf(false) }
    var offsetDisappearingAnimateContainer by remember { mutableStateOf(0.dp) }

    LaunchedEffect(isModalBottomSheetIsOpened) {
        if (isModalBottomSheetIsOpened) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible) {
            notifyBottomSheetWasClosed()
            keyboard?.hide()
            focusManager.clearFocus()
        }
    }

    ModalBottomSheetLayout(
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState,
        sheetBackgroundColor = Color.Transparent,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(SHEET_HEIGHT_COEFF)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen._20dp))
                    )
            ) {
                Spacer(
                    modifier = Modifier
                        .padding(top = dimensionResource(id = R.dimen._5dp))
                        .height(dimensionResource(id = R.dimen._5dp))
                        .width(dimensionResource(id = R.dimen._40dp))
                        .background(
                            color = PianoGray,
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen._5dp))
                        )
                        .align(Alignment.TopCenter)
                )
                Column(
                    Modifier
                        .padding(dimensionResource(id = R.dimen._20dp))
                        .verticalScroll(scrollStateModalSheet),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = (0.1).dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(shapeCornerRad)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = titleText,
                            onValueChange = { newText ->
                                titleText = newText
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = fontSizeTitle,
                                fontStyle = FontStyle(fontStyle.fontRes)
                            ),
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(R.dimen._5dp))
                                .fillMaxSize()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        isTextFocused = true
                                    } else {
                                        isTextFocused = false
                                    }
                                },
                            cursorBrush = SolidColor(PianoGray),
                        ) {
                            if (titleText.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.enter_title),
                                    style = ralewayExtraLightItalicTextStyle(
                                        size = dimensionResource(id = R.dimen._35sp),
                                        color = PianoGray
                                    ),
                                )
                            }
                            Text(
                                text = titleText.text,
                                style = TextStyle(
                                    fontSize = fontSizeTitle,
                                    color = Color.Black,
                                    fontFamily = FontFamily(Font(fontStyle.fontRes)),
                                ),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = padding)
                            .noRippleClickable {
                                clickToChangeFontStyle()
                            }
                            .fillMaxWidth()
                            .background(
                                color = Color.Black,
                                shape = RoundedCornerShape(shapeCornerRad)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(
                                    horizontal = padding,
                                    vertical = dimensionResource(R.dimen._5dp)
                                ),
                            text = stringResource(id = R.string.click_to_change_font_style),
                            style = TextStyle(
                                fontSize = fontSize,
                                color = Color.White,
                                fontFamily = FontFamily(Font(fontStyle.fontRes)),
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .noRippleClickable {
                                onClickExportJpeg(titleText.text)
                            }
                            .padding(top = padding)
                            .border(
                                width = (0.1).dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(shapeCornerRad)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(
                                    horizontal = padding,
                                    vertical = dimensionResource(R.dimen._5dp)
                                ),
                            text = stringResource(id = R.string.export_to_jpeg),
                            style = ralewayThinTextStyle(dimensionResource(R.dimen._20dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .noRippleClickable {
                                onClickExportPdf(titleText.text)
                            }
                            .padding(top = padding)
                            .border(
                                width = (0.1).dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(shapeCornerRad)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(
                                    horizontal = padding,
                                    vertical = dimensionResource(R.dimen._5dp)
                                ),
                            text = stringResource(id = R.string.export_to_pdf),
                            style = ralewayThinTextStyle(dimensionResource(R.dimen._20dp))
                        )
                    }
                }
            }
        }
    ) {
        Box {

            LazyColumn(
                modifier = Modifier
                    .padding(
                        top = padding,
                        start = padding,
                        end = padding
                    )
                    .fillMaxSize(),
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(compositions) { index, composition ->
                    var isClicked by remember { mutableStateOf(false) }

                    val animatedWidthFloat by animateFloatAsState(if (isClicked) WIDTH_COEFF else 1f)
                    val animatedButtonAlphaFloat by animateFloatAsState(if (isClicked) 1f else 0f)
                    val animatedHeightDp = remember { Animatable(height.value) }

                    Row(
                        modifier = Modifier
                            .height(animatedHeightDp.value.dp)
                    ) {
                        LibraryNoteString(
                            modifier = Modifier
                                .height(height)
                                .padding(bottom = padding)
                                .noRippleClickable {
                                    isClicked = !isClicked
                                }
                                .clip(RoundedCornerShape(shapeCornerRad))
                                .border(
                                    width = (0.1).dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(shapeCornerRad)
                                )
                                .fillMaxWidth(animatedWidthFloat),
                            notes = composition.notesPairs,
                            pianoConfiguration = pianoConfiguration
                        )

                        Column(
                            Modifier
                                .padding(bottom = padding)
                                .fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = animatedButtonAlphaFloat
                                    }
                                    .padding(
                                        start = dimensionResource(id = R.dimen._5dp),
                                        bottom = dimensionResource(id = R.dimen._5dp)
                                    )
                                    .fillMaxHeight(BUTTON_HEIGHT_COEFF)
                                    .fillMaxWidth()
                                    .clickable {
                                        isClicked = false
                                        onClickSaveComposition(composition)
                                    }
                                    .background(
                                        color = Color.Black,
                                        shape = RoundedCornerShape(shapeCornerRad)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = animatedButtonAlphaFloat
                                        },
                                    text = stringResource(id = R.string.common_save),
                                    style = ralewayMediumTextStyle(
                                        size = dimensionResource(id = R.dimen._12sp),
                                        color = Color.White
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(
                                        start = dimensionResource(id = R.dimen._5dp),
                                        bottom = dimensionResource(id = R.dimen._5dp)
                                    )
                                    .fillMaxHeight(BUTTON_SECOND_HEIGHT_COEFF)
                                    .fillMaxWidth()
                                    .clickable {
                                        isClicked = false
                                    }
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(shapeCornerRad)
                                    )
                                    .border(
                                        width = (0.1).dp, color = Color.Black,
                                        shape = RoundedCornerShape(shapeCornerRad)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            alpha = animatedButtonAlphaFloat
                                        },
                                    text = stringResource(id = R.string.common_edit),
                                    style = ralewayMediumTextStyle(
                                        size = dimensionResource(id = R.dimen._12sp),
                                        color = Color.Black
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(start = dimensionResource(id = R.dimen._5dp))
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            offsetDisappearingAnimateContainer =
                                                (index - lazyListState.firstVisibleItemIndex) * (height) -
                                                        lazyListState.firstVisibleItemScrollOffset.pxToDp(
                                                            context
                                                        )
                                            isClicked = false
                                            isAnimationStarted = false
                                            isAnimationStarted = true
                                            animatedHeightDp.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(
                                                    durationMillis = DELETING_DELAY.toInt(),
                                                    easing = EaseOutCubic
                                                )
                                            )
                                            delay(DELETING_DELAY)
                                            animatedHeightDp.snapTo(height.value)
                                            onClickDeleteComposition(composition)
                                        }
                                    }
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(shapeCornerRad)
                                    )
                                    .border(
                                        width = (0.1).dp, color = Red,
                                        shape = RoundedCornerShape(shapeCornerRad)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            alpha = animatedButtonAlphaFloat
                                        },
                                    text = stringResource(id = R.string.common_delete),
                                    style = ralewayMediumTextStyle(
                                        size = dimensionResource(id = R.dimen._12sp),
                                        color = Red
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .padding(
                                bottom = dimensionResource(id = R.dimen._5dp)
                            )
                            .height(dimensionResource(R.dimen._30dp))
                            .fillMaxWidth()
                            .clickable {
                                testSave()
                            }
                            .background(
                                color = Color.Black,
                                shape = RoundedCornerShape(shapeCornerRad)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier,
                            text = "add mocks",
                            style = handjetRegularTextStyle(
                                size = dimensionResource(id = R.dimen._20sp),
                                color = Color.White
                            )
                        )
                    }
                }
            }

            if (isAnimationStarted) {
                Box(
                    modifier = Modifier
                        .padding(
                            start = padding,
                            top = padding
                        )
                        .offset(y = offsetDisappearingAnimateContainer)
                ) {
                    DisappearingAnimateContainer(
                        isAnimationStarted = isAnimationStarted,
                        width = blockWidth.dp,
                        height = dimensionResource(R.dimen._100dp),
                        roundedBordersRadius = shapeCornerRad,
                        detailedByWidth = 80,
                        particlesCountCoeff = 1f,
                        speedParticles = 5L,
                        particleRadiusOffset = 0.2f,
                        onAnimationEnd = {
                            isAnimationStarted = false
                        }
                    )
                }
            }
        }
    }

    isTextFocused.IfTrue {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .noRippleClickable {
                    keyboard?.hide()
                    focusManager.clearFocus()
                    isTextFocused = false
                }
        )
    }
}

@FigmaLargePreview
@Composable
fun LibraryPagePreview() {
    LibraryPage(
        isModalBottomSheetIsOpened = false,
        compositions = listOf(
            MusicalComposition(),
            MusicalComposition()
        ),
        pianoConfiguration = PianoConfiguration(
            lineSpacing = 9f
        ),
        fontStyle = FontEnum.NUNITO_SANS_CONDENSED_EXTRA_LIGHT_ITALIC,

        testSave = {},
        notifyBottomSheetWasClosed = {},
        onClickSaveComposition = {},
        onClickDeleteComposition = {},
        clickToChangeFontStyle = {},
        onClickExportJpeg = {},
        onClickExportPdf = {}
    )
}