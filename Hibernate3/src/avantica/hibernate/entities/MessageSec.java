package avantica.hibernate.entities;

public class MessageSec {

	private Long id;
	private String text;
	private MessageSec nextMessage;
	
	public MessageSec(String text) {
		this.text = text;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public MessageSec getNextMessage() {
		return nextMessage;
	}
	
	public void setNextMessage(MessageSec nextMessage) {
		this.nextMessage = nextMessage;
	}
}