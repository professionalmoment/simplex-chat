//
//  CIMetaView.swift
//  SimpleX
//
//  Created by Evgeny Poberezkin on 11/02/2022.
//  Copyright © 2022 SimpleX Chat. All rights reserved.
//

import SwiftUI
import SimpleXChat

struct CIMetaView: View {
    @EnvironmentObject var chat: Chat
    var chatItem: ChatItem
    var metaColor = Color.secondary

    var body: some View {
        if chatItem.isDeletedContent {
            chatItem.timestampText.font(.caption).foregroundColor(metaColor)
        } else {
            let meta = chatItem.meta
            let ttl = chat.chatInfo.timedMessagesTTL
            switch meta.itemStatus {
            case .sndSent:
                ciMetaText(meta, chatTTL: ttl, color: metaColor, sent: .sent)
            case .sndRcvd:
                ZStack {
                    ciMetaText(meta, chatTTL: ttl, color: metaColor, sent: .rcvd1)
                    ciMetaText(meta, chatTTL: ttl, color: metaColor, sent: .rcvd2)
                }
            default:
                ciMetaText(meta, chatTTL: ttl, color: metaColor)
            }
        }
    }
}

enum SentCheckmark {
    case sent
    case rcvd1
    case rcvd2
}

func ciMetaText(_ meta: CIMeta, chatTTL: Int?, color: Color = .clear, transparent: Bool = false, sent: SentCheckmark? = nil) -> Text {
    var r = Text("")
    if meta.itemEdited {
        r = r + statusIconText("pencil", color)
    }
    if meta.disappearing {
        r = r + statusIconText("timer", color).font(.caption2)
        let ttl = meta.itemTimed?.ttl
        if ttl != chatTTL {
            r = r + Text(shortTimeText(ttl)).foregroundColor(color)
        }
        r = r + Text(" ")
    }
    if let (icon, statusColor) = meta.statusIcon(color) {
        let t = Text(Image(systemName: icon)).font(.caption2)
        let gap = Text("  ").kerning(-1.25)
        let t1 = t.foregroundColor(transparent ? .clear : statusColor.opacity(0.67))
        switch sent {
        case nil: r = r + t1
        case .sent: r = r + t1 + gap
        case .rcvd1: r = r + t.foregroundColor(transparent ? .clear : color.opacity(0.67)) + gap
        case .rcvd2: r = r + gap + t1
        }
        r = r + Text(" ")
    } else if !meta.disappearing {
        r = r + statusIconText("circlebadge.fill", .clear) + Text(" ")
    }
    return (r + meta.timestampText.foregroundColor(color)).font(.caption)
}

private func statusIconText(_ icon: String, _ color: Color) -> Text {
    Text(Image(systemName: icon)).foregroundColor(color)
}

struct CIMetaView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            CIMetaView(chatItem: ChatItem.getSample(2, .directSnd, .now, "https://simplex.chat", .sndSent))
            CIMetaView(chatItem: ChatItem.getSample(2, .directSnd, .now, "https://simplex.chat", .sndSent, itemEdited: true))
            CIMetaView(chatItem: ChatItem.getDeletedContentSample())
        }
        .previewLayout(.fixed(width: 360, height: 100))
        .environmentObject(Chat.sampleData)
    }
}
