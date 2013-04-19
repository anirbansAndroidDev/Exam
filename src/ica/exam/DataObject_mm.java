package ica.exam;

public class DataObject_mm {
	 private String RowId = "";
	 private int AnsweredId = 0;
	 private String AnswerAttribute1 = "";
	 private String AnswerAttribute2 = "";
	 private String AnswerAttribute3 = "";

	 public DataObject_mm(String rowid, int answererid, String attribute1, String attribute2, String attribute3) {
			this.RowId = rowid;
			this.AnsweredId = answererid;
			this.AnswerAttribute1 = attribute1;
			this.AnswerAttribute2 = attribute2;
			this.AnswerAttribute3 = attribute3;
		}

	 public void setRowId(String rowid) {
		 this.RowId = rowid;
	 	}

	 public String getRowId() {
		 return RowId;
	 	}

	 public void setAnsweredId(int answeredid) {
		 this.AnsweredId = answeredid;
	 	}

	 public int getAnsweredId() {
		 return AnsweredId;
	 	}
	 
	 public void setAnswerAttribute1(String answerattribute1) {
		 this.AnswerAttribute1 = answerattribute1;
	 	}

	 public String getAnswerAttribute1() {
		 return AnswerAttribute1;
	 	}

	 public void setAnswerAttribute2(String answerattribute2) {
		  this.AnswerAttribute2 = answerattribute2;
		 }

	 public String getAnswerAttribute2() {
		  return AnswerAttribute2;
		 }

	 public void setAnswerAttribute3(String answerattribute3) {
		  this.AnswerAttribute3 = answerattribute3;
		 }

	 public String getAnswerAttribute3() {
		  return AnswerAttribute3;
		 }
}