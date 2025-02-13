package chat.simplex.app.views.newchat

import SectionBottomSpacer
import android.content.ClipboardManager
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import chat.simplex.app.R
import chat.simplex.app.TAG
import chat.simplex.app.model.ChatModel
import chat.simplex.app.ui.theme.*
import chat.simplex.app.views.helpers.*
import chat.simplex.res.MR

@Composable
fun PasteToConnectView(chatModel: ChatModel, close: () -> Unit) {
  val connectionLink = remember { mutableStateOf("") }
  val context = LocalContext.current
  val clipboard = getSystemService(context, ClipboardManager::class.java)
  PasteToConnectLayout(
    chatModel.incognito.value,
    connectionLink = connectionLink,
    pasteFromClipboard = {
      connectionLink.value = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context) as? String ?: return@PasteToConnectLayout
    },
    connectViaLink = { connReqUri ->
      try {
        val uri = Uri.parse(connReqUri)
        withUriAction(uri) { linkType ->
          val action = suspend {
            Log.d(TAG, "connectViaUri: connecting")
            if (connectViaUri(chatModel, linkType, uri)) {
              close()
            }
          }
          if (linkType == ConnectionLinkType.GROUP) {
            AlertManager.shared.showAlertDialog(
              title = generalGetString(MR.strings.connect_via_group_link),
              text = generalGetString(MR.strings.you_will_join_group),
              confirmText = generalGetString(MR.strings.connect_via_link_verb),
              onConfirm = { withApi { action() } }
            )
          } else action()
        }
      } catch (e: RuntimeException) {
        AlertManager.shared.showAlertMsg(
          title = generalGetString(MR.strings.invalid_connection_link),
          text = generalGetString(MR.strings.this_string_is_not_a_connection_link)
        )
      }
    },
  )
}

@Composable
fun PasteToConnectLayout(
  chatModelIncognito: Boolean,
  connectionLink: MutableState<String>,
  pasteFromClipboard: () -> Unit,
  connectViaLink: (String) -> Unit,
) {
  Column(
    Modifier.verticalScroll(rememberScrollState()).padding(horizontal = DEFAULT_PADDING),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    AppBarTitle(stringResource(MR.strings.connect_via_link), false)
    Text(stringResource(MR.strings.paste_connection_link_below_to_connect))

    InfoAboutIncognito(
      chatModelIncognito,
      true,
      generalGetString(MR.strings.incognito_random_profile_from_contact_description),
      generalGetString(MR.strings.profile_will_be_sent_to_contact_sending_link)
    )

    Box(Modifier.padding(top = DEFAULT_PADDING, bottom = 6.dp)) {
      TextEditor(connectionLink, Modifier.height(180.dp), contentPadding = PaddingValues())
    }

    Row(
      Modifier.fillMaxWidth().padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.Start,
    ) {
      if (connectionLink.value == "") {
        SimpleButton(text = stringResource(MR.strings.paste_button), icon = painterResource(MR.images.ic_content_paste)) {
          pasteFromClipboard()
        }
      } else {
        SimpleButton(text = stringResource(MR.strings.clear_verb), icon = painterResource(MR.images.ic_close)) {
          connectionLink.value = ""
        }
      }
      Spacer(Modifier.weight(1f).fillMaxWidth())
      SimpleButton(text = stringResource(MR.strings.connect_button), icon = painterResource(MR.images.ic_link)) {
        connectViaLink(connectionLink.value)
      }
    }

    Text(annotatedStringResource(MR.strings.you_can_also_connect_by_clicking_the_link))
    SectionBottomSpacer()
  }
}


@Preview(showBackground = true)
@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES,
  name = "Dark Mode"
)
@Composable
fun PreviewPasteToConnectTextbox() {
  SimpleXTheme {
    PasteToConnectLayout(
      chatModelIncognito = false,
      connectionLink = remember { mutableStateOf("") },
      pasteFromClipboard = {},
      connectViaLink = { link ->
        try {
          println(link)
  //        withApi { chatModel.controller.apiConnect(link) }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      },
    )
  }
}
