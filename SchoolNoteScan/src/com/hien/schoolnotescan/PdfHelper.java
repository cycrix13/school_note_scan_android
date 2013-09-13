package com.hien.schoolnotescan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
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
	
	public static String RenderPdf(List<String> list, String name, Context ct) {
		Document doc = new Document();
		File file = null;
		try {
			file = ct.getFileStreamPath(name);
			Log.d("PDFCreator", "PDF Path: " + file.getAbsolutePath());
			FileOutputStream fOut = new FileOutputStream(file);

			PdfWriter.getInstance(doc, fOut);
			doc.open();
			addTitlePage(doc);
			
			for (int i = 0; i < (int) list.size(); i++) {
				Image myImg = Image.getInstance(ct.getFileStreamPath(list.get(i)).getAbsolutePath());
				myImg.setAlignment(Image.MIDDLE);
				doc.add(myImg);
				Paragraph preface = new Paragraph();
				addEmptyLine(preface, 1);
				doc.add(preface);
			}
		} catch (DocumentException de) {
			Log.e("PDFCreator", "DocumentException:" + de);
			return "";
		} catch (IOException e) {
			Log.e("PDFCreator", "ioException:" + e);
			return "";
		} finally {
			doc.close();
		}
		return file.getAbsolutePath();
	}
	
	public static String RenderPdf(List<String> list, String name, Context ct, int dummy) {
		Document doc = new Document();
		try {
			FileOutputStream fOut = new FileOutputStream(name);

			PdfWriter.getInstance(doc, fOut);
			doc.open();
			addTitlePage(doc);
			
			for (int i = 0; i < (int) list.size(); i++) {
				Image myImg = Image.getInstance(ct.getFileStreamPath(list.get(i)).getAbsolutePath());
				myImg.setAlignment(Image.MIDDLE);
				doc.add(myImg);
				Paragraph preface = new Paragraph();
				addEmptyLine(preface, 1);
				doc.add(preface);
			}
		} catch (DocumentException de) {
			Log.e("PDFCreator", "DocumentException:" + de);
			return "";
		} catch (IOException e) {
			Log.e("PDFCreator", "ioException:" + e);
			return "";
		} finally {
			doc.close();
		}
		return name;
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
