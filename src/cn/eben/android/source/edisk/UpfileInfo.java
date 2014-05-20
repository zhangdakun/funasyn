package cn.eben.android.source.edisk;

public class UpfileInfo {
	public String luid;
	public String etag;
	public long size;
	public String durl;
	public String path;
	public String parent;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
//		return super.toString();
		StringBuilder sb = new StringBuilder();
		sb.append("luid: ").append(luid).append(",etag: ").append(etag).
		append(",size: ").append(size).append(",durl: ")
		.append(durl).append(",path: ").append(path).append(",parent: ").append(parent);
		
		return sb.toString();
	}

	public UpfileInfo(String luid, String etag, long size, String durl) {
		super();
		this.luid = luid;
		this.etag = etag;
		this.size = size;
		this.durl = durl;
	}
	
	public UpfileInfo(String luid, String etag, long size, String durl,
			String path, String parent) {
		super();
		this.luid = luid;
		this.etag = etag;
		this.size = size;
		this.durl = durl;
		this.path = path;
		this.parent = parent;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	
	
}
