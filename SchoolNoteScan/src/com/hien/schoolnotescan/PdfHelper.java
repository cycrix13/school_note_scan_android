package com.hien.schoolnotescan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfHelper {
	private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 30,
		      Font.BOLD);
	public static boolean RenderPdf(List<Bitmap> list, String name) {
		Document doc = new Document();
		try {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/droidText";

			File dir = new File(path);
			if (!dir.exists())
				dir.mkdirs();
			Log.d("PDFCreator", "PDF Path: " + path);
			File file = new File(dir, name);
			FileOutputStream fOut = new FileOutputStream(file);

			PdfWriter.getInstance(doc, fOut);
			doc.open();
			addTitlePage(doc);
			
			for (int i = 0; i < (int) list.size(); i++) {
				Bitmap bitmap = list.get(i);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				Image myImg = Image.getInstance(stream.toByteArray());
				myImg.setAlignment(Image.MIDDLE);
				doc.add(myImg);
				Paragraph preface = new Paragraph();
				addEmptyLine(preface, 1);
				doc.add(preface);
			}
		} catch (DocumentException de) {
			Log.e("PDFCreator", "DocumentException:" + de);
			return false;
		} catch (IOException e) {
			Log.e("PDFCreator", "ioException:" + e);
			return false;
		} finally {
			doc.close();
		}
		return true;
	}

	private static void addTitlePage(Document document)
			throws DocumentException {
		Paragraph preface = new Paragraph();
		// We add one empty line
		addEmptyLine(preface, 1);
		preface.add(new Paragraph("School Note Scan", catFont));
		document.add(preface);
	}

	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}
}
