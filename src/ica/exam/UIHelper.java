package ica.exam;

public class UIHelper {
	   public static boolean homeKeyPressed;
	    private static boolean justLaunched = true;

	    public static void checkJustLaunced() {
	        if (justLaunched) {
	            homeKeyPressed = true;
	            justLaunched = false;
	        } else {
	            homeKeyPressed = false;
	        }
	    }

	    public static void checkHomeKeyPressed(boolean killSafely) {
	        if (homeKeyPressed) {
	            killApp(true);
	        } else {
	            homeKeyPressed = true;
	        }
	    }

	    public static void killApp(boolean killSafely) {
	        if (killSafely) {
	            System.runFinalizersOnExit(true);
	            System.exit(0);
	        } else {
	            android.os.Process.killProcess(android.os.Process.myPid());
	        }

	    }

}
