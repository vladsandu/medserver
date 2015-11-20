package medproject.medserver.databaseHandler;

public enum StoredProcedure {
	OperatorLookup("{call operatorLookup(?,?)}", false);

	private StoredProcedure(String sql, boolean updatingRequest) {
		this.sql = sql;
		this.updatingRequest = updatingRequest;
	}
	
	private final String sql;
	private final boolean updatingRequest;
	
	public boolean isUpdatingRequest() {
		return updatingRequest;
	}
	
	public String getSQL(){
		return sql;
	}
	
}
