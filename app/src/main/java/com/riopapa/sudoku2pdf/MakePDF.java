package com.riopapa.sudoku2pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.core.content.res.ResourcesCompat;

import com.riopapa.sudoku2pdf.Model.SudokuInfo;

class MakePDF {

    static void createPDF(String [] blankTables, String [] answerTables, SudokuInfo sudokuInfo) {

        String downLoadFolder = Environment.getExternalStorageDirectory().getPath();
        String fileName = MainActivity.fileDate;
        Bitmap sigMap = BitmapFactory.decodeResource(sudokuInfo.context.getResources(), R.mipmap.my_sign_blured);
        int xSig = sigMap.getWidth() / 5;
        int ySig = sigMap.getHeight() / 5;
        sigMap = Bitmap.createScaledBitmap(sigMap, xSig, ySig, false);
        int meshType = sudokuInfo.meshType;
        int pgWidth = 210*5, pgHeight = 297*5;  // A4 size
        int twoThree = sudokuInfo.twoThree;
        int boxWidth = (twoThree == 2) ? pgHeight / (11*2) : pgHeight / (11*3);
        int boxWidth3 = boxWidth / 3;
        int space = boxWidth*2/3;  // space = 2 : 24, 3:
        int pageNbr = 0;
        Canvas canvas = null;
        PdfDocument.Page page = null;
        PdfDocument document = new PdfDocument();

        Paint pRect = new Paint();      // inner box    (quiz)
        pRect.setColor(Color.BLUE);
        pRect.setStyle(Paint.Style.STROKE);
        pRect.setStrokeWidth(1);
        pRect.setAlpha(200);
        pRect.setPathEffect(new DashPathEffect(new float[] {1,2}, 0));

        Paint pRectO = new Paint();     // outer box
        pRectO.setColor(Color.DKGRAY);
        pRectO.setStyle(Paint.Style.STROKE);
        pRectO.setStrokeWidth(2);

        Paint pDotted = new Paint();        // inner dotted box (answer)
        pDotted.setPathEffect(new DashPathEffect(new float[] {6, 3}, 0));
        pDotted.setColor(Color.BLUE);
        pDotted.setAlpha(120);
        pDotted.setStrokeWidth(1);

        Paint pNumb = new Paint();
        pNumb.setColor(Color.BLUE);
        pNumb.setAlpha(180);
        pNumb.setStrokeWidth(1);
        pNumb.setTypeface(ResourcesCompat.getFont(sudokuInfo.context, R.font.good_times));
        pNumb.setTextSize(boxWidth * 8 / 10);
        pNumb.setTextAlign(Paint.Align.CENTER);
        pNumb.setStyle(Paint.Style.FILL_AND_STROKE);

        Paint pMemo = new Paint();
        pMemo.setColor(Color.BLUE);
        pMemo.setStyle(Paint.Style.STROKE);
        pMemo.setStrokeWidth(0);
        pMemo.setTextAlign(Paint.Align.CENTER);
        pMemo.setStyle(Paint.Style.FILL_AND_STROKE);
        pMemo.setTextSize(32);

        Paint pSig = new Paint();
        pSig.setColor(Color.BLACK);
        pSig.setTextAlign(Paint.Align.RIGHT);
        pSig.setStyle(Paint.Style.STROKE);
        pSig.setStrokeWidth(0);
        pSig.setAlpha(180);
        pSig.setStyle(Paint.Style.FILL_AND_STROKE);
        pSig.setTextSize(24);

        for (int idx = 0; idx < blankTables.length; idx++) {
            int [][] xyTable = str2suArray(blankTables[idx]);
            if (idx % twoThree == 0) {
                if (idx != 0) {
                    printSignature(sudokuInfo, sigMap, pgWidth, pgHeight, canvas, pSig, true);
                    document.finishPage(page);
                }
                pageNbr++;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
                // start a page
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
            }
            int xBase = boxWidth + 10;
            int yBase = space + 10 + (idx % twoThree) * boxWidth * 11;
            int xGap = boxWidth/2;
            int yGap = boxWidth3+boxWidth3+boxWidth3/3;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, pRect);
                    if (xyTable[row][col] > 0) {
                        canvas.drawText("" + xyTable[row][col], xPos + xGap, yPos + yGap, pNumb);
                    } else if (meshType == 1){
                        canvas.drawLine(xPos + boxWidth3, yPos, xPos + boxWidth3, yPos + boxWidth3, pDotted);
                        canvas.drawLine(xPos + boxWidth3 + boxWidth3, yPos,
                                xPos + boxWidth3 + boxWidth3, yPos + boxWidth3, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3, pDotted);
                    } else if (meshType == 2){
                        canvas.drawLine(xPos + boxWidth3, yPos, xPos + boxWidth3, yPos + boxWidth, pDotted);
                        canvas.drawLine(xPos + boxWidth3 + boxWidth3, yPos,
                                xPos + boxWidth3 + boxWidth3, yPos + boxWidth, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3 + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3 + boxWidth3, pDotted);
                    }
                }
            }

            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, pRectO);
                }
            canvas.drawText("("+idx+")", xBase + 32 + boxWidth*9, yBase + boxWidth/2f, pMemo);
        }
        printSignature(sudokuInfo, sigMap, pgWidth, pgHeight, canvas, pSig, true);

        document.finishPage(page);

        File filePath = new File(downLoadFolder, fileName+" Qz.pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }
        // close the document
        document.close();

        if (!sudokuInfo.makeAnswer)
            return;

//         Create Answer Page ------------

        document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, 1).create();
        // start a page
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();

        boxWidth = (pgWidth - space) / 40;    // 4 answer for 1 line
        pNumb.setTextSize(boxWidth/2f); // pNumb : answer number
        pNumb.setAlpha(180);
        pMemo.setTextSize(boxWidth/2f);  // pMemo : given number
        pMemo.setColor(Color.DKGRAY);
        pMemo.setAlpha(200);

        for (int nbrInPage = 0; nbrInPage < answerTables.length; nbrInPage++) {
            int [][] ansTable = str2suArray(answerTables[nbrInPage]);
            int [][] blankTable = str2suArray(blankTables[nbrInPage]);
            int xBase = space +20 + (nbrInPage % 4) * boxWidth * 95 / 10;
            int yBase = space + (nbrInPage / 4) * boxWidth * 11;
            int xGap = boxWidth/2;
            int yGap = boxWidth*3/4;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, pRect);
                    canvas.drawText("" + ansTable[row][col], xPos + xGap, yPos + yGap,
                            (blankTable[row][col] == 0)? pNumb:pMemo);
                }
            }
            canvas.drawText("["+nbrInPage+"]", xBase + boxWidth, yBase - 8, pMemo);

            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, pRectO);
                }
        }

        printSignature(sudokuInfo, sigMap, pgWidth, pgHeight, canvas, pSig, false);
        document.finishPage(page);

        // write the document content
        filePath = new File(downLoadFolder, fileName+"Ans.pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }
        // close the document
        document.close();
    }

    private static void printSignature(SudokuInfo sudokuInfo, Bitmap sigMap, int pgWidth, int pgHeight, Canvas canvas, Paint paint, boolean top) {
        int inc = (int) paint.getTextSize() * 4 / 3;
        if (top) {
            int xPos = pgWidth - 40;
            int yPos = 40;
            canvas.drawText(sudokuInfo.dateTime.substring(0,8),xPos, yPos, paint);
            yPos += inc;
            canvas.drawText(sudokuInfo.dateTime.substring(9),xPos, yPos, paint);
            yPos += inc;
            canvas.drawText("□ "+sudokuInfo.blankCount,xPos, yPos, paint);
            yPos += inc;
            xPos -= sigMap.getWidth();
            canvas.drawBitmap(sigMap, xPos, yPos, paint);
        } else {
            int xPos = pgWidth/2;
            int yPos = pgHeight - 50;
            String s = sudokuInfo.dateTime + "   □"+ sudokuInfo.blankCount;
            canvas.drawText(sudokuInfo.dateTime, xPos, yPos, paint);
            xPos += inc;
            yPos -= sigMap.getHeight()/2;
            canvas.drawBitmap(sigMap, xPos, yPos, paint);
        }
    }

    static int [][] str2suArray(String str) {
        String [] sRow = str.split(":");
        int [][] sudoku = new int [9][9];
        for (int row = 0; row < 9; row++) {
            String[] sCol = sRow[row].split(";");
            for (int col = 0; col < 9; col++)
                sudoku[row][col] = Integer.parseInt(sCol[col]);
        }
        return sudoku;
    }
}