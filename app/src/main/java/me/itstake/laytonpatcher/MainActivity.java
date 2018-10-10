package me.itstake.laytonpatcher;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements FileChooserDialog.FileCallback {
    boolean isMinimized = false;
    public static String notificationResult = "";

    public static native int xdelta3PatchRom(String romPath, String patchPath, String outputFile);

    private byte[] createChecksum(String filename) {
        InputStream fis = null;
        try {
            fis = new FileInputStream(filename);

            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            fis.close();
            return complete.digest();
        } catch (FileNotFoundException e) {
            //stream for writing text
            FileWriter writer = null;
            try {
                writer = new FileWriter(getExternalFilesDir(null).getAbsolutePath() + "/log.log");
                writer.write(e.getLocalizedMessage());
                if (writer != null) writer.close();
            } catch (Throwable t) {
            }
        } catch (NoSuchAlgorithmException e) {
            //stream for writing text
            FileWriter writer = null;
            try {
                writer = new FileWriter(getExternalFilesDir(null).getAbsolutePath() + "/log.log");
                writer.write(e.getLocalizedMessage());
                if (writer != null) writer.close();
            } catch (Throwable t) {
            }
        } catch (IOException e) {
            //stream for writing text
            FileWriter writer = null;
            try {
                writer = new FileWriter(getExternalFilesDir(null).getAbsolutePath() + "/log.log");
                writer.write(e.getLocalizedMessage());
                if (writer != null) writer.close();
            } catch (Throwable t) {
            }
        }
        return null;
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public String getMD5Checksum(String filename) {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);
        try {
            System.loadLibrary("xdelta3patcher");
        } catch (UnsatisfiedLinkError e) {
            Log.e("Bad:", "Cannot grab xdelta3patcher:" + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isMinimized = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMinimized = false;
    }

    public void onCardClick(View view) {
        final MainActivity activity = this;
        if (view instanceof CardView) {
            int id = view.getId();
            if (id == R.id.ltt_patch) {
                new MaterialDialog.Builder(this)
                        .title("더빙 언어 선택")
                .content("어떤 언어로 더빙을 즐길지 선택해 주세요.")
                .positiveText("한국어")
                .negativeText("영어")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PERMISSION_GRANTED) {
                                System.out.println("Permission is granted");
                                Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
                                new FileChooserDialog.Builder(activity)
                                        .chooseButton(R.string.patch)
                                        .tag("ltt_romchooser")
                                        .show();
                            } else {
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
                            new FileChooserDialog.Builder(activity)
                                    .chooseButton(R.string.patch)
                                    .tag("ltt_romchooser")
                                    .show();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Toast.makeText(getApplicationContext(), "현재 버전은 영어 더빙을 지원하지 않습니다.", Toast.LENGTH_LONG).show();
                        /*
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PERMISSION_GRANTED) {
                                System.out.println("Permission is granted");
                                Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
                                new FileChooserDialog.Builder(activity)
                                        .chooseButton(R.string.patch)
                                        .tag("ltt_romchooser_en")
                                        .show();
                            } else {
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
                            new FileChooserDialog.Builder(activity)
                                   .chooseButton(R.string.patch)
                                   .tag("ltt_romchooser_en")
                                   .show();
                        }
                    */
                    }
                })
                .show();
            } else if (id == R.id.ls_patch) {
                new MaterialDialog.Builder(this)
                        .title("더빙 언어 선택")
                        .content("어떤 언어로 더빙을 즐길지 선택해 주세요.")
                        .positiveText("한국어")
                        .negativeText("영어")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (Build.VERSION.SDK_INT >= 23) {
                                    if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            == PERMISSION_GRANTED) {
                                        System.out.println("Permission is granted");
                                        Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
                                        new FileChooserDialog.Builder(activity)
                                                .chooseButton(R.string.patch)
                                                .tag("ls_romchooser")
                                                .show();
                                    } else {
                                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
                                    new FileChooserDialog.Builder(activity)
                                            .chooseButton(R.string.patch)
                                            .tag("ls_romchooser")
                                            .show();
                                }
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (Build.VERSION.SDK_INT >= 23) {
                                    if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            == PERMISSION_GRANTED) {
                                        System.out.println("Permission is granted");
                                        Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
                                        new FileChooserDialog.Builder(activity)
                                                .chooseButton(R.string.patch)
                                                .tag("ls_romchooser_en")
                                                .show();
                                    } else {
                                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
                                    new FileChooserDialog.Builder(activity)
                                            .chooseButton(R.string.patch)
                                            .tag("ls_romchooser_en")
                                            .show();
                                }
                            }
                        })
                .show();
            } else if (id == R.id.go_blog) {
                startActivity(new Intent(this, BlogActivity.class));
            } else if(id == R.id.info) {
                startActivity(new Intent(this, InfoActivity.class));
            }
        }
    }

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull final File original) {
        // TODO
        final String tag = dialog.getTag();
        System.out.println(tag + "," + original.getAbsolutePath());

        if (tag.equals("ltt_romchooser")) {
            MaterialDialog waitdialog = new MaterialDialog.Builder(this)
                    .title(R.string.wait_patch)
                    .content(R.string.wait_patch_description)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .show();
            waitdialog.setCancelable(false);
            waitdialog.setCanceledOnTouchOutside(false);
            new PatchTask(this, original, 3, true, waitdialog).execute();
        } else if (tag.equals("ls_romchooser")) {
            MaterialDialog waitdialog = new MaterialDialog.Builder(this)
                    .title(R.string.wait_patch)
                    .content(R.string.wait_patch_description)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .show();
            waitdialog.setCancelable(false);
            waitdialog.setCanceledOnTouchOutside(false);
            new PatchTask(this, original, 4, true, waitdialog).execute();
        } else if (tag.equals("ltt_romchooser_en")) {
            MaterialDialog waitdialog = new MaterialDialog.Builder(this)
                    .title(R.string.wait_patch)
                    .content(R.string.wait_patch_description)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .show();
            waitdialog.setCancelable(false);
            waitdialog.setCanceledOnTouchOutside(false);
            new PatchTask(this, original, 3, false, waitdialog).execute();
        } else if (tag.equals("ls_romchooser_en")) {
            MaterialDialog waitdialog = new MaterialDialog.Builder(this)
                    .title(R.string.wait_patch)
                    .content(R.string.wait_patch_description)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .show();
            waitdialog.setCancelable(false);
            waitdialog.setCanceledOnTouchOutside(false);
            new PatchTask(this, original, 4, false, waitdialog).execute();
        }
    }

    private String patch(File original, int patch, boolean kd) throws Exception {
        String lttmd5 = "bd35908d4dc01a85c8847f6b71975551";
        String lsmd5 = "0711856d982d5cd9544a2fab347e76cf";
        if (patch == 3) {
            copyPatchlt3(kd);
            if (!getMD5Checksum(original.getAbsolutePath()).equals(lttmd5)) {
                return getResources().getString(R.string.incorrect_rom);
            }
        } else if (patch == 4) {
            copyPatchlt4(kd);
            if (!getMD5Checksum(original.getAbsolutePath()).equals(lsmd5)) {
                return getResources().getString(R.string.incorrect_rom);
            }
        }
        StringBuilder b = new StringBuilder(original.getName());
        b.replace(original.getName().lastIndexOf(".nds"), original.getName().lastIndexOf(".nds") + 4, "_KOR.nds");
        String newfilename = b.toString();
        final File patched = new File(original.getParent() + "/" + newfilename);
        String msg = "저장된 경로:\n" + patched.getAbsolutePath();
        System.out.println("File Patch Started");
        int e;
        if (kd) {
            e = xdelta3PatchRom(original.getAbsolutePath(), new File(getFilesDir() + "/layton" + patch + "kor.diff").getAbsolutePath(), patched.getAbsolutePath());
        } else {
            e = xdelta3PatchRom(original.getAbsolutePath(), new File(getFilesDir() + "/layton" + patch + "eng.diff").getAbsolutePath(), patched.getAbsolutePath());
        }
        if (e != 0) {
            switch (e) {
                case -17710:
                    msg = getResources().getString(R.string.xdelta3NegativeSeventeenThousandSevenHundredTen);
                case -17711:
                    msg = getResources().getString(R.string.xdelta3NegativeSeventeenThousandSevenHundredEleven);
                case -17712:
                    msg = getResources().getString(R.string.xdelta3NegativeSeventeenThousandSevenHundredTwelve);
                default:
                    msg = getResources().getString(R.string.xdelta3Default) + e;
            }

        }
        return msg;
    }


    private void copyPatchlt3(boolean koreandub) {
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            if(koreandub) {
                in = assetManager.open("layton3kor.diff");
                out = new FileOutputStream(getFilesDir() + "/layton3kor.diff");
            } else {
                in = assetManager.open("layton3eng.diff");
                out = new FileOutputStream(getFilesDir() + "/layton3eng.diff");
            }
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            if(koreandub) {
                System.out.println("Patch path:" + getFilesDir() + "/layton3kor.diff" + " exists:" + new File(getFilesDir() + "/layton3kor.diff").exists());
            } else {
                System.out.println("Patch path:" + getFilesDir() + "/layton3eng.diff" + " exists:" + new File(getFilesDir() + "/layton3eng.diff").exists());
            }
        } catch (Exception e) {
            //stream for writing text
            FileWriter writer = null;
            try {
                writer = new FileWriter(getExternalFilesDir(null).getAbsolutePath() + "/log.log");
                writer.write(e.getLocalizedMessage());
                if (writer != null) writer.close();
            } catch (Throwable t) {
            }
        }
    }

    private void copyPatchlt4(boolean koreandub) {
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            if(koreandub) {
                in = assetManager.open("layton4kor.diff");
                out = new FileOutputStream(getFilesDir() + "/layton4kor.diff");
            } else {
                in = assetManager.open("layton4eng.diff");
                out = new FileOutputStream(getFilesDir() + "/layton4eng.diff");
            }
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            if(koreandub) {
                System.out.println("Patch path:" + getFilesDir() + "/layton4kor.diff" + " exists:" + new File(getFilesDir() + "/layton4kor.diff").exists());
            } else {
                System.out.println("Patch path:" + getFilesDir() + "/layton4eng.diff" + " exists:" + new File(getFilesDir() + "/layton4eng.diff").exists());
            }
        } catch (Exception e) {
            //stream for writing text
            FileWriter writer = null;
            try {
                writer = new FileWriter(getExternalFilesDir(null).getAbsolutePath() + "/log.log");
                writer.write(e.getLocalizedMessage());
                if (writer != null) writer.close();
            } catch (Throwable t) {
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private class PatchTask extends AsyncTask<String, String, String> {
        private Context con = null;
        private File original = null;
        private int version = 0;
        private MaterialDialog waitdialog = null;
        private boolean koreandub = true;

        public PatchTask(Context context, File ori, int ver, boolean kd, MaterialDialog wd) {
            con = context;
            original = ori;
            version = ver;
            waitdialog = wd;
            koreandub = kd;
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return patch(original, version, koreandub);
            } catch (Exception e) {
                //stream for writing text
                FileWriter writer = null;
                try {
                    writer = new FileWriter(getExternalFilesDir(null).getAbsolutePath() + "/log.log");
                    writer.write(e.getLocalizedMessage());
                    if (writer != null) writer.close();
                    return "패치 도중 에러입니다. 로그 경로:\n" + getExternalFilesDir(null).getAbsolutePath() + "/log.log";
                } catch (Throwable t) {
                    return "패치 도중 에러입니다.\n로그를 저장할 수 없습니다.";
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            waitdialog.dismiss();
            System.out.println("File Patch Ended");
            if (!isMinimized) {
                MaterialDialog show = new MaterialDialog.Builder(con)
                        .title(R.string.patch_result)
                        .content(result)
                        .positiveText(R.string.ok)
                        .show();
            } else {
                notificationResult = result;
                android.support.v4.app.NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(con)
                                .setSmallIcon(R.mipmap.notification)
                                .setContentTitle(getResources().getString(R.string.patch_result))
                                .setContentText(result);
                Intent resultIntent = new Intent(con, NotificationPopupActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(con);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                builder.setContentIntent(resultPendingIntent);
                builder.setVibrate(new long[] { 500 });
                builder.setLights(Color.rgb(79, 55, 48), 2000, 2000);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, builder.build());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showFileChooser(3, true);
                } else {
                    System.out.println("Permission Revoked");
                    Toast.makeText(getApplicationContext(), "권한 사용을 승인하셔야 패치를 계속하실 수 있습니다.", Toast.LENGTH_LONG).show();
                }
                break;
            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showFileChooser(3, false);
                } else {
                    Toast.makeText(getApplicationContext(), "권한 사용을 승인하셔야 패치를 계속하실 수 있습니다.", Toast.LENGTH_LONG).show();
                }
                break;
            case 3:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showFileChooser(4, true);
                } else {
                    Toast.makeText(getApplicationContext(), "권한 사용을 승인하셔야 패치를 계속하실 수 있습니다.", Toast.LENGTH_LONG).show();
                }
                break;
            case 4:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showFileChooser(4, false);
                } else {
                    Toast.makeText(getApplicationContext(), "권한 사용을 승인하셔야 패치를 계속하실 수 있습니다.", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    private void showFileChooser(int mode, boolean koreandub) {
        Toast.makeText(getApplicationContext(), "패치할 nds 파일을 선택해 주세요.", Toast.LENGTH_LONG).show();
        if(mode == 3) {
            if(koreandub) {
                new FileChooserDialog.Builder(this)
                        .chooseButton(R.string.patch)
                        .tag("ltt_romchooser")
                        .show();
            } else {
                new FileChooserDialog.Builder(this)
                        .chooseButton(R.string.patch)
                        .tag("ltt_romchooser_en")
                        .show();
            }
        } else if(mode == 4) {
            if(koreandub) {
                new FileChooserDialog.Builder(this)
                        .chooseButton(R.string.patch)
                        .tag("ls_romchooser")
                        .show();
            } else {
                new FileChooserDialog.Builder(this)
                        .chooseButton(R.string.patch)
                        .tag("ls_romchooser_en")
                        .show();
            }
        }
    }
}
