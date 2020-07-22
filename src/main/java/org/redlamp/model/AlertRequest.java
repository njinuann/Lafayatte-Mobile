package org.redlamp.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AlertRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	private String name;
	private String subject;
	private String content_type = "text/html; charset=utf-8";

	private String body, channel;
	private String status;
	private String callback_url;

	private String chargeable;
	private String charge_account;

	private String scheduled;
	private String cron_pattern;
	private boolean repeat;

	private String has_attachment;

	private String type_name;
	private String folder;
	private long sender_id;

	private long group_id;
	private long recipient_id;

	private long author_id;
	private String recipient;

	private long row_version;

	private long template_id;
	private Map<String, Object> template_params = new HashMap<String, Object>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent_type() {
		return content_type;
	}

	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCallback_url() {
		return callback_url;
	}

	public void setCallback_url(String callback_url) {
		this.callback_url = callback_url;
	}

	public String getChargeable() {
		return chargeable;
	}

	public void setChargeable(String chargeable) {
		this.chargeable = chargeable;
	}

	public String getCharge_account() {
		return charge_account;
	}

	public void setCharge_account(String charge_account) {
		this.charge_account = charge_account;
	}

	public String getScheduled() {
		return scheduled;
	}

	public void setScheduled(String scheduled) {
		this.scheduled = scheduled;
	}

	public String getCron_pattern() {
		return cron_pattern;
	}

	public void setCron_pattern(String cron_pattern) {
		this.cron_pattern = cron_pattern;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public String getHas_attachment() {
		return has_attachment;
	}

	public void setHas_attachment(String has_attachment) {
		this.has_attachment = has_attachment;
	}

	public String getType_name() {
		return type_name;
	}

	public void setType_name(String type_name) {
		this.type_name = type_name;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public long getSender_id() {
		return sender_id;
	}

	public void setSender_id(long sender_id) {
		this.sender_id = sender_id;
	}

	public long getGroup_id() {
		return group_id;
	}

	public void setGroup_id(long group_id) {
		this.group_id = group_id;
	}

	public long getRecipient_id() {
		return recipient_id;
	}

	public void setRecipient_id(long recipient_id) {
		this.recipient_id = recipient_id;
	}

	public long getAuthor_id() {
		return author_id;
	}

	public void setAuthor_id(long author_id) {
		this.author_id = author_id;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public long getRow_version() {
		return row_version;
	}

	public void setRow_version(long row_version) {
		this.row_version = row_version;
	}

	public long getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(long template_id) {
		this.template_id = template_id;
	}

	public Map<String, Object> getTemplate_params() {
		return template_params;
	}

	public void setTemplate_params(Map<String, Object> template_params) {
		this.template_params = template_params;
	}

	public void addTemplateParam(String param, Object value) {
		this.template_params.put(param, value);
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
