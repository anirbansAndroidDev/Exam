package ica.ICAConstants;

import static ica.exam.ExamActivity.DownloadMockExamStatusCode;
import static ica.exam.ExamActivity.DownloadStudyMatStatusCode;
import static ica.exam.ExamActivity.DownloadPracticeExamStatusCode;
import static ica.exam.ExamActivity.MockExamStatusCode;

;

public enum CourseMatIntent {

	DownlaodMock(DownloadMockExamStatusCode), StudyMaterials(
			DownloadStudyMatStatusCode), PracticeExam(
			DownloadPracticeExamStatusCode), MockExam(MockExamStatusCode);

	private int number;

	CourseMatIntent(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public static CourseMatIntent fromInteger(int x) {

		switch (x) {
		case DownloadMockExamStatusCode:
			return CourseMatIntent.DownlaodMock;

		case DownloadPracticeExamStatusCode:
			return CourseMatIntent.PracticeExam;

		case DownloadStudyMatStatusCode:
			return CourseMatIntent.StudyMaterials;
		case MockExamStatusCode:
			return CourseMatIntent.MockExam;		
		default:

			return CourseMatIntent.DownlaodMock;
		}

	}

}