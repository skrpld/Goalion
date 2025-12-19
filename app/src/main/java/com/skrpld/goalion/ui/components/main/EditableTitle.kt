package com.skrpld.goalion.ui.components.main

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditableTitle(
    title: String,
    isEditing: Boolean,
    isDone: Boolean,
    onTitleChange: (String) -> Unit,
    onEditDone: () -> Unit,
    textStyle: TextStyle,
    placeholder: String
) {
    val focusRequester = remember { FocusRequester() }
    val isKeyboardVisible = WindowInsets.isImeVisible

    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = title))
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            textFieldValueState = TextFieldValue(
                text = title,
                selection = TextRange(title.length)
            )
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(title) {
        if (textFieldValueState.text != title) {
            textFieldValueState = textFieldValueState.copy(text = title)
        }
    }

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && isEditing) {
            onEditDone()
        }
    }

    if (isEditing) {
        BasicTextField(
            value = textFieldValueState,
            onValueChange = {
                textFieldValueState = it
                onTitleChange(it.text)
            },
            textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onEditDone() }),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )
    } else {
        Text(
            text = title.ifEmpty { placeholder },
            style = textStyle.copy(
                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                color = if (isDone) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}