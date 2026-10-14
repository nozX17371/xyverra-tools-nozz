package com.nozx.xyverra;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(8,13,24), PANEL = Color.rgb(17,26,43), CYAN = Color.rgb(0,229,255), WHITE = Color.rgb(235,245,255), MUTED = Color.rgb(145,165,190);
    private LinearLayout root, content, nav;
    private TextView status;
    private File selectedScript;
    private static final int PICK_FILE = 41;

    @Override public void onCreate(Bundle b) { super.onCreate(b); showPage("HOME"); }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); t.setPadding(dp(4),dp(5),dp(4),dp(5)); return t;
    }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private void addSpace(LinearLayout l, int h) { View v = new View(this); l.addView(v, new LinearLayout.LayoutParams(1,dp(h))); }
    private void card(String title, String desc, String button, Runnable action) {
        LinearLayout c = column(); c.setPadding(dp(14),dp(10),dp(14),dp(12)); c.setBackgroundColor(PANEL);
        TextView h = text(title,17,CYAN,true); c.addView(h); c.addView(text(desc,13,MUTED,false));
        if (button != null) { Button b = btn(button); b.setOnClickListener(v -> action.run()); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,dp(44)); p.topMargin=dp(8); c.addView(b,p); }
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(10); content.addView(c,p);
    }
    private Button btn(String s) { Button b = new Button(this); b.setText(s); b.setTextColor(WHITE); b.setAllCaps(false); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0,105,130))); return b; }
    private void showPage(String page) {
        root = column(); root.setBackgroundColor(BG); root.setPadding(dp(16),dp(12),dp(16),dp(8));
        TextView brand = text("XYVERRA  /  nozX PROJECT",22,CYAN,true); root.addView(brand);
        root.addView(text("VERSION 1.0   •   FREE FIRE TOOLS",11,MUTED,true)); addSpace(root,8);
        ScrollView scroll = new ScrollView(this); content = column(); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        status = text("STATUS: READY",12,MUTED,false);
        if (page.equals("HOME")) {
            content.addView(text("GAMING CONTROL CENTER",20,WHITE,true));
            content.addView(text("Dashboard perangkat dan pengelola file lokal.",13,MUTED,false)); addSpace(content,12);
            card("HEADLOCK 70%", "Slot konfigurasi lokal — tidak mengubah aim atau memodifikasi game.", "PILIH FILE KONFIGURASI", () -> pickFile(false));
            card("EASY DRAG", "Slot profil kontrol/sensitivitas untuk referensi manual.", "BUKA TOOLS", () -> showPage("TOOLS"));
            card("HEADTRICK", "Slot profil latihan dan catatan pengaturan.", "BUKA PROFIL", () -> showPage("TOOLS"));
            card("GAME FILES", "Pilih file milik sendiri untuk dikelola di ruang aplikasi.", "PILIH FILE", () -> pickFile(false));
        } else if (page.equals("TOOLS")) {
            content.addView(text("TOOLS & PERFORMANCE",20,WHITE,true));
            content.addView(text("Pengaturan di sini tidak mengubah sensitivitas internal Free Fire secara otomatis.",13,MUTED,false)); addSpace(content,10);
            addSlider("SENSITIVITAS X", "X"); addSlider("SENSITIVITAS Y", "Y");
            card("OPTIMASI HP", "Buka panel aplikasi Android untuk mengelola izin dan penggunaan baterai.", "INFO OPTIMASI", () -> toast("Gunakan pengaturan Android untuk optimasi perangkat."));
            card("GAME BOOST", "Mode praktis: tutup aplikasi yang tidak diperlukan secara manual.", "TIPS BOOST", () -> toast("Aktifkan mode performa dari pengaturan perangkat jika tersedia."));
            card("CLEAR CACHE", "Menghapus cache Xyverra saja; cache Free Fire tidak disentuh.", "CLEAR APP CACHE", () -> clearOwnCache());
            card("CROSSHAIR", "Overlay memerlukan izin tampil di atas aplikasi lain dan kompatibilitas perangkat.", "PENGATURAN OVERLAY", () -> { Intent i = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())); try { startActivity(i); } catch(Exception e) { toast("Pengaturan overlay tidak tersedia."); } });
            card("SPEEDOMETER", "Monitor status sederhana. FPS game dan suhu hardware tidak tersedia untuk semua perangkat.", "CEK STATUS", () -> { status.setText("STATUS: APP ACTIVE • ANDROID"); toast("Monitor aplikasi aktif; FPS game tidak dibaca langsung."); });
        } else {
            content.addView(text("XYVERRA TERMINAL",20,WHITE,true));
            content.addView(text("Skrip dijalankan dengan shell Android dalam sandbox aplikasi. Skrip tidak mendapat akses root dan tidak meng-inject game.",13,MUTED,false)); addSpace(content,8);
            card("SCRIPT MANAGER", "Pilih file .sh dari penyimpanan menggunakan file picker Android.", "PILIH FILE .SH", () -> pickFile(true));
            card("RUN SCRIPT", selectedScript == null ? "Belum ada skrip yang dipilih." : "Terpilih: "+selectedScript.getName(), "JALANKAN DI SANDBOX", () -> runScript());
            card("APP FILES", "Folder privat aplikasi: "+getFilesDir().getAbsolutePath(), "LIHAT LOKASI", () -> toast(getFilesDir().getAbsolutePath()));
            content.addView(text("LOG",15,CYAN,true));
            content.addView(status);
        }
        nav = row(); nav.setPadding(0,dp(8),0,0);
        String[] pages={"HOME","TOOLS","TERMINAL"}; for(String p:pages){ Button b=btn(p); b.setTextSize(11); nav.addView(b,new LinearLayout.LayoutParams(0,dp(48),1)); b.setOnClickListener(v->showPage(p)); }
        root.addView(nav); setContentView(root);
    }
    private void addSlider(String title, String key) {
        LinearLayout c=column(); c.setPadding(dp(12),dp(8),dp(12),dp(8)); c.setBackgroundColor(PANEL);
        TextView label=text(title+"  •  0–100",14,WHITE,true); c.addView(label); SeekBar sb=new SeekBar(this); sb.setMax(100); sb.setProgress(50); sb.setProgressTintList(android.content.res.ColorStateList.valueOf(CYAN)); sb.setThumbTintList(android.content.res.ColorStateList.valueOf(CYAN)); c.addView(sb); sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int v,boolean u){label.setText(title+"  •  "+v);} public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}});
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(10); content.addView(c,p);
    }
    private void pickFile(boolean scriptOnly) {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("*/*"); i.addCategory(Intent.CATEGORY_OPENABLE); i.putExtra(Intent.EXTRA_TITLE, scriptOnly?"Pilih file .sh":"Pilih file konfigurasi");
        try { startActivityForResult(i,PICK_FILE); } catch(Exception e){ toast("File picker tidak tersedia."); }
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) { super.onActivityResult(requestCode,resultCode,data); if(requestCode==PICK_FILE && resultCode==RESULT_OK && data!=null){ Uri uri=data.getData(); if(uri==null)return; String name="selected_file"; try { String path=uri.getLastPathSegment(); if(path!=null && path.contains(":")) name=path.substring(path.lastIndexOf(':')+1); } catch(Exception ignored){}
        if(name.isEmpty())name="selected_file"; final String safeName=name.replaceAll("[^A-Za-z0-9._-]","_"); File dest=new File(getFilesDir(),safeName);
        try(InputStream in=getContentResolver().openInputStream(uri); FileOutputStream out=new FileOutputStream(dest)){ if(in==null)throw new IOException("Tidak dapat membaca file"); byte[] buf=new byte[8192]; int n; while((n=in.read(buf))>0)out.write(buf,0,n); selectedScript=dest; toast("File disalin: "+safeName); showPage("TERMINAL"); } catch(Exception e){ toast("Gagal menyalin file: "+e.getMessage()); }
    }}
    private void runScript(){ if(selectedScript==null || !selectedScript.exists()){ toast("Pilih file .sh terlebih dahulu."); return; } if(!selectedScript.getName().toLowerCase().endsWith(".sh")){ toast("Hanya file .sh yang bisa dijalankan dari menu ini."); return; }
        new Thread(()->{ String result; try { Process p=new ProcessBuilder("/system/bin/sh",selectedScript.getAbsolutePath()).directory(getFilesDir()).redirectErrorStream(true).start(); ByteArrayOutputStream out=new ByteArrayOutputStream(); InputStream in=p.getInputStream(); byte[] buf=new byte[2048]; int n; while((n=in.read(buf))!=-1){ if(out.size()<16000)out.write(buf,0,n); } int code=p.waitFor(); result="Selesai (exit "+code+")\n"+new String(out.toByteArray(),StandardCharsets.UTF_8); } catch(Exception e){ result="Gagal menjalankan skrip: "+e.getMessage(); } final String r=result; runOnUiThread(()->new android.app.AlertDialog.Builder(this).setTitle("Script Log").setMessage(r).setPositiveButton("OK",null).show()); }).start();
    }
    private void clearOwnCache(){ File d=getCacheDir(); deleteChildren(d); toast("Cache Xyverra dibersihkan."); }
    private void deleteChildren(File f){ File[] files=f.listFiles(); if(files!=null)for(File x:files){ if(x.isDirectory())deleteChildren(x); x.delete(); } }
    private void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
}
