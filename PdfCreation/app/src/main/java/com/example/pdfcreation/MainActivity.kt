package com.example.pdfcreation

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var retriveImageBitmap: Bitmap
    lateinit var scaleDownBitmap: Bitmap
   lateinit var directory_path:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        getStoragePermission()

        button_pdf_create.setOnClickListener {
            createPdf()
        }
    }

    private fun getStoragePermission() {


        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            );

        }
    }

    @SuppressLint("IntentReset")
    private fun createPdf() {
        var pdfDocument = PdfDocument()
        var displayHeight = Resources.getSystem().displayMetrics.heightPixels
        var displayWidth = Resources.getSystem().displayMetrics.widthPixels

        var mPaint = Paint()

        var mPageInfo = PdfDocument.PageInfo.Builder(displayWidth, displayHeight, 1).create()

        var mCreatePage = pdfDocument.startPage(mPageInfo)

        var mCreatePageCanvus = mCreatePage.canvas

        retriveImageBitmap = BitmapFactory.decodeResource(resources, R.drawable.pizza_heading)

        scaleDownBitmap = Bitmap.createScaledBitmap(retriveImageBitmap, displayWidth, 200, false);

        mCreatePageCanvus.drawBitmap(scaleDownBitmap, 0f, 0f, mPaint)

        //draw text as title in middle
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        mPaint.textSize = 40f
        mCreatePageCanvus.drawText("YarDly PiZZa ", displayWidth / 2f, 120f, mPaint)

        //set contact number left side
        mPaint.textSize = 16f
        mPaint.setColor(resources.getColor(R.color.white, theme))
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC))
        mCreatePageCanvus.drawText("Phone: 01643-567115", 10f, 40f, mPaint)

        //set invoice text below image
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        mPaint.textSize = 40f
        mPaint.setColor(Color.rgb(73, 72, 77))
        mCreatePageCanvus.drawText("INVOICE", displayWidth / 2f, 210f, mPaint)

        //set customer details
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        mPaint.textSize = 16f
        mPaint.setColor(Color.rgb(37, 76, 92))
        mCreatePageCanvus.drawText("Customer Name: S.M. Zahidul Islam", 10f, 260f, mPaint)
        mCreatePageCanvus.drawText("Customer Cell: 01720192586", 10f, 280f, mPaint)

        //set invoice number and time
        val date: String = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())
        val currentTime = Calendar.getInstance().time
        mPaint.textAlign = Paint.Align.RIGHT
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        mPaint.textSize = 16f
        mPaint.setColor(Color.rgb(37, 76, 92))
        mCreatePageCanvus.drawText("Invoice Number: 000-20-0306", displayWidth - 10f, 260f, mPaint)
        mCreatePageCanvus.drawText("Date: $date", displayWidth - 10f, 280f, mPaint)
        mCreatePageCanvus.drawText("Time: $currentTime", displayWidth - 10f, 300f, mPaint)


        //draw rectangular line

        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 2f
        mCreatePageCanvus.drawRect(10f, 320f, displayWidth - 10f, 350f, mPaint)


        //set text inside box
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        mPaint.textSize = 16f
        mPaint.setColor(Color.rgb(37, 76, 92))

        mCreatePageCanvus.drawText("Sl No", 10f, 322f, mPaint)
        mCreatePageCanvus.drawText("Product Name", (displayWidth / 8f) + 5f, 322f, mPaint)
        mCreatePageCanvus.drawText("Price", (displayWidth / 2f) + 5f, 322f, mPaint)
        mCreatePageCanvus.drawText("Quantity", (displayWidth / 1.5f) + 5f, 322f, mPaint)
        mCreatePageCanvus.drawText(
            "T. Price",
            displayWidth - (displayWidth / 8f + 5f),
            322f,
            mPaint
        )


        //create horizontal line
        mCreatePageCanvus.drawLine(displayWidth / 8f, 320f, displayWidth / 8f, 350f, mPaint)
        mCreatePageCanvus.drawLine(displayWidth / 2f, 320f, displayWidth / 2f, 350f, mPaint)
        mCreatePageCanvus.drawLine(displayWidth / 1.5f, 320f, displayWidth / 1.5f, 350f, mPaint)
        mCreatePageCanvus.drawLine(
            displayWidth - (displayWidth / 8f),
            320f,
            displayWidth - (displayWidth / 8f),
            350f,
            mPaint
        )


        //set item list 1
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        mPaint.textSize = 16f
        mPaint.setColor(Color.rgb(37, 76, 92))

        mCreatePageCanvus.drawText("1", 10f, 360f, mPaint)
        mCreatePageCanvus.drawText("Italina", (displayWidth / 8f) + 5f, 360f, mPaint)
        mCreatePageCanvus.drawText("800.00", (displayWidth / 2f) + 5f, 360f, mPaint)
        mCreatePageCanvus.drawText("1", (displayWidth / 1.5f) + 5f, 360f, mPaint)
        mCreatePageCanvus.drawText("800.00", displayWidth - (displayWidth / 8f + 5f), 360f, mPaint)


        //set item list 2
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        mPaint.textSize = 16f
        mPaint.setColor(Color.rgb(37, 76, 92))

        mCreatePageCanvus.drawText("2", 10f, 385f, mPaint)
        mCreatePageCanvus.drawText("Manilai", (displayWidth / 8f) + 5f, 385f, mPaint)
        mCreatePageCanvus.drawText("900.00", (displayWidth / 2f) + 5f, 385f, mPaint)
        mCreatePageCanvus.drawText("1", (displayWidth / 1.5f) + 5f, 385f, mPaint)
        mCreatePageCanvus.drawText("900.00", displayWidth - (displayWidth / 8f + 5f), 385f, mPaint)


        //total area
        mCreatePageCanvus.drawLine(
            displayWidth / 2f,
            displayHeight - 200f,
            displayWidth - 10f,
            displayHeight - 200f,
            mPaint
        )
        mPaint.textAlign = Paint.Align.RIGHT
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        mPaint.textSize = 16f
        mPaint.setColor(Color.rgb(37, 76, 92))
        mCreatePageCanvus.drawText(
            "Sub Total    :    1700.00 BDT",
            displayWidth - 10f,
            displayHeight - 190f,
            mPaint
        )
        mCreatePageCanvus.drawText(
            "Vat(2%)      :      34.00 BDT",
            displayWidth - 10f,
            displayHeight - 170f,
            mPaint
        )
        mCreatePageCanvus.drawText(
            "Total        :    1734.00 BDT",
            displayWidth - 10f,
            displayHeight - 150f,
            mPaint
        )

        mPaint.setColor(resources.getColor(R.color.purple_200, theme))
        mCreatePageCanvus.drawRect(
            displayWidth / 2f,
            displayHeight - 155f,
            displayWidth - 10f,
            displayHeight - 135f,
            mPaint
        )


        pdfDocument.finishPage(mCreatePage)


        var haveFun=File(getExternalFilesDir("nothingPossible"),"myTxt.txt")

        try {


            var savedData:String="This file is saved when press button by text document"

            var fileOutputStream=FileOutputStream(haveFun)
            fileOutputStream.write(savedData.toByteArray())
            fileOutputStream.close()
            var stringBuilder: StringBuilder= StringBuilder()
            var fileReader=FileReader(haveFun)
            var bufferedReader=BufferedReader(fileReader)
            while (bufferedReader.readLine()!=null){
                stringBuilder.append(bufferedReader.readLine()+"\n")
            }
            Toast.makeText(this,stringBuilder.toString(),Toast.LENGTH_LONG).show()

        }catch (e: Exception){

            Toast.makeText(this,e.localizedMessage,Toast.LENGTH_LONG).show()

        }

        if (android.os.Build.VERSION.SDK_INT >29){
            directory_path  =Environment.getExternalStorageDirectory().getPath() + "/Download"

        }else{

            directory_path = Environment.getExternalStorageDirectory().path+"/mypdf"
        }

        val file = File(directory_path)
        if (!file.exists()) {
            file.mkdirs()
        }

        val targetPdf = file.path+ "/test-2.pdf"/*directory_path + "/test-2.pdf"*/
        val filePath = File(targetPdf)
        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            Log.e("main", "error " + e.toString())
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show()
        }
        // close the document
        // close the document
        pdfDocument.close()


    /*    var intent=Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(this, AUTHORITY, filePath))
        intent.setType("application/pdf")
        //intent.setData(Uri.fromFile(filePath))
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this, "No application found",
                Toast.LENGTH_SHORT
            ).show()
        }*/

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            101 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_LONG).show()
                    Log.e("value", "Permission Granted, Now you can use local drive .")
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .")
                }
            }
        }
    }
}