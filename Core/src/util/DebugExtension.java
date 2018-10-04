package util;

public interface DebugExtension 
{
	public void exceptionCaught(boolean showError);
	public void unableToEmailLogs();
	public void sendEmail(String title, String message) throws Exception;
}
