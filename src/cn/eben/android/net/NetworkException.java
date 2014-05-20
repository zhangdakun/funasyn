package cn.eben.android.net;

public class NetworkException extends Exception {
    /**
	 * 
	 */
//	private static final long serialVersionUID = 1L;
	/** The code of the exception */
    private int code;
    
    public NetworkException(int code, String msg) {
		super(msg);
		this.code = code;
	}

	/** Returns the code of this exception */
    public int getCode() {
        return code;
    }
    
}
