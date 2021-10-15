package org.redlamp.extras;

public class ErrorHandler {

	public static String mapCode(int returnCode) {
		String RC;
		switch (returnCode) {
		case 0:
			RC = "00";
			break;
		case 10:
			RC = "53";
			break;
		case 11:
			RC = "52";
			break;
		case 28:
			RC = "52";
			break;
		case 24:
			RC = "14";
			break;
		case 30:
			RC = "13";
			break;
		case 39:
			RC = "51";
			break;
		case 43:
			RC = "25";
			break;
		case 51:
			RC = "45";
			break;
		case 58:
		case -50040:
			RC = "58";
			break;
		case 60:
			RC = "13";
			break;
		case 70:
			RC = "26";
			break;
		case 79:
			RC = "13";
			break;
		case 111:
			RC = "57";
			break;
		case -50007:
			RC = "40";
			break;
		default:
			RC = "96";
			break;
		}
		return RC;
	}

}
