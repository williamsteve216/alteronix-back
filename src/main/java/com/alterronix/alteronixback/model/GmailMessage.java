package com.alterronix.alteronixback.model;

import com.alterronix.alteronixback.entity.AIReply;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GmailMessage {
    private String id;
    private String threadId;
    private String subject;
    private String fromName;
    private String fromEmail;
    private String snippet;
    private String date;
    private boolean isRead;
    private boolean isPriority;
    private boolean isReply;
    private List<AIReply> replyList;
}
