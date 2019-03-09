
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import jdk.nashorn.internal.runtime.options.Options;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gruhayn
 */
public class Functions {
    
    private static Connection conn=null;
    private static Statement stmt=null;
    private static ResultSet rs = null;
    private static PreparedStatement pr=null;
    static boolean close() {
        try { if (rs != null || !rs.isClosed()) {
            rs.close();
        } } catch (SQLException e) {}
        try { if (stmt != null || !stmt.isCloseOnCompletion() || !stmt.isClosed()) {
            stmt.close();
        } } catch (SQLException e) {}
        try { if (conn != null || !conn.isClosed()) {
            conn.close();
        } } catch (SQLException e) {}
        
        return true;
    }
    public String getCurrentYear()
    {    
        try {
            qosul("ogrenci");
            stmt = conn.createStatement();
            rs= stmt.executeQuery("SELECT `CURRENTYEAR` FROM `PROGRAM` WHERE 1");
            if(rs.next())
            {
                String a =rs.getString("CURRENTYEAR");                
                close();
                return a;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
        close();
        return null;
    }
    
    public static void qosul(String file)
    {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:databases/"+file+".db");
            while(conn==null || conn.isClosed())
            {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:databases/"+file+".db");
                System.out.println("qosulmadi");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String[] getYariyillar()
    {
        String []yariyil;
        System.out.println();/*
        String currentDir = this.getClass().getClassLoader().getResource("").getPath();
        System.out.println(currentDir);
        System.out.println("");*/
        File file = new File("databases/");

        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
        if(directories.length==1)
        {
            yariyil=new String[1];
            yariyil[0]="";
            return yariyil;
        }
        else
        {
            int k=1;
            yariyil=new String[directories.length];
            yariyil[0]="";
            for(String i :directories)
            {   
                if(!i.equals("ogrenci.db"))
                {
                    yariyil[k]=i.substring(0, i.length()-3);
                    k++;
                }
            }
            return yariyil;
        }
    }
    
    public void yeniYariyilEkle(String yariyil) throws IOException
    {  
        String in = "databases/ogrenci.db";
        String out = "databases/"+yariyil+".db";
        File source = new File(in);
        File dest = new File(out);
        Files.copy(source.toPath(), dest.toPath());
    }

    public void setActiveYariyil(String newYariyil) 
    {
        try {
            qosul("ogrenci");
            stmt = conn.createStatement(); 
            String query="UPDATE  `PROGRAM`  SET  `CURRENTYEAR`='"+newYariyil+"'";
            stmt.executeUpdate(query);
            close();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();            
        }
    }

    public String[] get(String grup1) {
        String [] ads = null;
        try {
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            if(grup1.equals("GRUP")) {
                rs= stmt.executeQuery("SELECT `AD` FROM `GRUPLAR`");
            }
            if(grup1.equals("SINIF"))
            {
                rs= stmt.executeQuery("SELECT `AD` FROM `SINIFLAR`");                
            }
            ArrayList <String> ad=new ArrayList<>();
            while(rs.next())
            {
                ad.add(rs.getString("AD"));
            }
            
            if(ad.size()>0)
            {
                ads= new String[ad.size()+1];
                ads[0]= "";
                int k=1;
                for(String i : ad)
                {
                    ads[k]=i;
                    k++;
                }
                close();
                return ads;
            }
            close();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
        close();
        ads= new String[1];
        ads[0]="";
        return ads;
    }
    public String getOgrenciNumarasiString()
    {
        try {
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            rs= stmt.executeQuery("SELECT `OGRNO` FROM `PROGRAM`");                
            if(rs.next())
            {
                String a=rs.getString("OGRNO");
                close();
                return a;
            }
            close();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
        close();
        return null;
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    public boolean yeniOgrencininBilgileriniSistemeAt(YeniKayitEkle yke,boolean duzenle)
    {
        try {
            if(!duzenle)
            {
                qosul(getCurrentYear());
                stmt = conn.createStatement();
                String query="INSERT INTO `OGRENCILER` VALUES('"+yke.getjLabelOgrenciNumarasi().getText()+"',"
                    +" '"+yke.getjTextFieldOgrenciAd().getText()+"', "
                    +" '"+yke.getjTextFieldOgrenciSoyad().getText() +"', "
                    +" '"+yke.getjTextFieldOgrenciTelNumarasi().getText() +"', "
                    +" '"+yke.getjTextFieldOgrenciTel1().getText() +"', "
                    +" '"+yke.getjTextFieldOgrenciTel1().getText() +"', "
                    +" '"+yke.getjTextFieldOgrenciAdres().getText() +"', "
                    +" '"+String.valueOf(yke.getjComboBoxGrubuOgrenciDurumu().getSelectedItem()) +"', "
                    +" '"+yke.getDateChooserComboDogumTarihi().getText() +"', "
                    +" '"+yke.getDateChooserComboKayitTarihi().getText() +"', "
                    +" '"+yke.getjTextFieldGeldigiOkul().getText() +"', "
                    +" '"+String.valueOf(yke.getjComboBoxGrubu1().getSelectedItem()) +"', "
                    +" '"+String.valueOf(yke.getjComboBoxSinifi1().getSelectedItem()) +"', "
                    +" '"+String.valueOf(yke.getjComboBoxGrubu2().getSelectedItem()) +"', "
                    +" '"+String.valueOf(yke.getjComboBoxSinifi2().getSelectedItem()) +"', "
                    +" '"+yke.getjTextFieldAnneAd().getText() +"', "
                    +" '"+yke.getjTextFieldAnneSoyad().getText() +"', "
                    +" '"+yke.getjTextFieldAnneTelNo().getText() +"', "
                    +" '"+yke.getjTextFieldAnneTel1().getText() +"', "
                    +" '"+yke.getjTextFieldAnneTel2().getText() +"', "
                    +" '"+yke.getjTextFieldBabaAd().getText() +"', "
                    +" '"+yke.getjTextFieldBabaSoyad().getText() +"', "
                    +" '"+yke.getjTextFieldBabaTelNo().getText() +"', "
                    +" '"+yke.getjTextFieldBabaTel1().getText() +"', "
                    +" '"+yke.getjTextFieldBabaTel2().getText() +"' )";

                stmt.executeUpdate(query);
                close();
                return true;
            }
            else
            {         
            String query="UPDATE `OGRENCILER` SET `AD`=?, `SOYAD`=?, `TELNO`=?, `TEL1`=? WHERE `NO`=?";   
            qosul(getCurrentYear());
            pr = conn.prepareStatement(query);
                pr.setString(1, yke.getjTextFieldOgrenciAd().getText());
                pr.setString(2, yke.getjTextFieldOgrenciSoyad().getText());
                pr.setString(3, yke.getjTextFieldOgrenciTelNumarasi().getText());
                pr.setString(4, yke.getjTextFieldOgrenciTel1().getText());
                pr.setString(5, yke.getjLabelOgrenciNumarasi().getText());
            pr.execute();
            close();
            
            query="UPDATE `OGRENCILER` SET `TEL2`=?,`ADRES`=?, `OGRENCIDURUMU`=?,  `DOGUMTARIHI`=? WHERE `NO`=?";   
            qosul(getCurrentYear());
            pr = conn.prepareStatement(query);
                pr.setString(1, yke.getjTextFieldOgrenciTel2().getText());
                pr.setString(2, yke.getjTextFieldOgrenciAdres().getText());
                pr.setString(3, String.valueOf(yke.getjComboBoxGrubuOgrenciDurumu().getSelectedItem()));
                pr.setString(4, yke.getDateChooserComboDogumTarihi().getText());
                pr.setString(5, yke.getjLabelOgrenciNumarasi().getText());
            pr.execute();
            close();
            
            query="UPDATE `OGRENCILER` SET `KAYITTARIHI`=?,  `GELDIGIOKUL`=?, `GRUP1`=?,  `SINIF1`=? WHERE `NO`=?";   
            qosul(getCurrentYear());
            pr = conn.prepareStatement(query);
                pr.setString(1, yke.getDateChooserComboKayitTarihi().getText());
                pr.setString(2, yke.getjTextFieldGeldigiOkul().getText());
                pr.setString(3, String.valueOf(yke.getjComboBoxGrubu1().getSelectedItem()));
                pr.setString(4, String.valueOf(yke.getjComboBoxSinifi1().getSelectedItem()));
                pr.setString(5, yke.getjLabelOgrenciNumarasi().getText());
            pr.execute();
            close();   
            
            query="UPDATE `OGRENCILER` SET `GRUP2`=?,  `SINIF2`=?, `ANNEAD`=?,  `ANNESOYAD`=? WHERE `NO`=?";   
            qosul(getCurrentYear());
            pr = conn.prepareStatement(query);
                pr.setString(1, String.valueOf(yke.getjComboBoxGrubu2().getSelectedItem()));
                pr.setString(2, String.valueOf(yke.getjComboBoxSinifi2().getSelectedItem()));
                pr.setString(3, yke.getjTextFieldAnneAd().getText());
                pr.setString(4, yke.getjTextFieldAnneSoyad().getText());
                pr.setString(5, yke.getjLabelOgrenciNumarasi().getText());
            pr.execute();
            close();
            
            query="UPDATE `OGRENCILER` SET `ANNETELNO`=?,  `ANNETEL1`=?,  `ANNETEL2`=?,  `BABAAD`=? WHERE `NO`=?";   
            qosul(getCurrentYear());
            pr = conn.prepareStatement(query);
                pr.setString(1, yke.getjTextFieldAnneTelNo().getText());
                pr.setString(2, yke.getjTextFieldAnneTel1().getText());
                pr.setString(3, yke.getjTextFieldAnneTel2().getText());
                pr.setString(4, yke.getjTextFieldBabaAd().getText());
                pr.setString(5, yke.getjLabelOgrenciNumarasi().getText());
            pr.execute();
            close();
            
            query="UPDATE `OGRENCILER` SET `BABASOYAD`=?, `BABATELNO`=?, `BABATEL1`=?,`BABATEL2`=? WHERE `NO`=?";   
            qosul(getCurrentYear());
            pr = conn.prepareStatement(query);
                pr.setString(1, yke.getjTextFieldBabaSoyad().getText());
                pr.setString(2, yke.getjTextFieldBabaTelNo().getText());
                pr.setString(3, yke.getjTextFieldBabaTel1().getText());
                pr.setString(4, yke.getjTextFieldBabaTel2().getText());
                pr.setString(5, yke.getjLabelOgrenciNumarasi().getText());
            pr.execute();
            close();             
            return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
        close();
        return false;
        
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////
    
    
    public void artirOgrenciNumarasi(String numara)
    {
        try {
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE `PROGRAM` SET `OGRNO`=+'"+String.valueOf((Integer.parseInt(numara)+1))+"' WHERE 1");
            close();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
    }

    public String[][] getTable(String[] basliq,String dbName) {
        try {
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT count(*) from `"+dbName+"`");
            int boyut=0;
            if(rs.next())
            {
                boyut=rs.getInt(1);
            }
            close();
            if(boyut>0)
            {
                qosul(getCurrentYear());
                stmt = conn.createStatement();
                String query="SELECT ";
                int i;
                for(i=0;i<basliq.length-1;i++)
                {
                    query=query+" `"+basliq[i]+"`,";
                }
                query=query+" `"+basliq[i]+"`"+" FROM `"+dbName+"` where 1";
                
                rs= stmt.executeQuery(query);
                String[][] tableModelMatrix= new String[boyut][basliq.length+1];
                for(i=0;i<boyut;i++)
                {
                    rs.next();
                    int j;
                    for(j=0;j<basliq.length;j++)
                    {
                        tableModelMatrix[i][j]=rs.getString(j+1);
                    }
                }
                close();
                return tableModelMatrix;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            close();
        }
        close();
        return null;
        
    }

    String[][] filtrele(String[][] matrix, String[] basliq, String[] secilenler, Integer[] yerler) {
        String[][] filtrelenmisMatrix= new String[matrix.length][basliq.length];
        boolean yukle=true;
        int iF=0;
        for(int iMatrix = 0; iMatrix < matrix.length ;iMatrix++ )
        {   
            int jYerler;
            yukle=true;
            for(jYerler = 0 ; jYerler <yerler.length;jYerler++)
            {
                if(!secilenler[jYerler].equals(""))
                {
                    if(yerler[jYerler]>9 && yerler[jYerler]<100)
                    {
                        if((!matrix[iMatrix][yerler[jYerler]/10].equals(secilenler[jYerler])) && (!matrix[iMatrix][yerler[jYerler]%10].equals(secilenler[jYerler])))
                        {
                            yukle=false;
                        }
                    }
                    else if(yerler[jYerler]>99)
                    {
                        if((!matrix[iMatrix][yerler[jYerler]/100].contains(secilenler[jYerler])) && (!matrix[iMatrix][yerler[jYerler]%10].contains(secilenler[jYerler]) && (!matrix[iMatrix][(yerler[jYerler]/10)%10].contains(secilenler[jYerler]))))
                        {
                            yukle=false;
                        }
                    }
                    else
                    {
                        System.out.println(matrix[0].length);
                        if(!(matrix[iMatrix][yerler[jYerler]].toLowerCase().contains(secilenler[jYerler].toLowerCase())))
                        {
                            yukle=false;
                        }
                    }
                }
            }
            
            if(yukle)
            {  
                for(int j=0;j<basliq.length;j++)
                {
                    filtrelenmisMatrix[iF][j]=matrix[iMatrix][j];
                }
                iF++;
            }
        }
        
        return filtrelenmisMatrix;
    }

    public void setYeniKayitEkleDuzenle(YeniKayitEkle yke,String ogrNo) {
        try {
            yke.setComboBoxs();
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            rs=stmt.executeQuery("SELECT * FROM `OGRENCILER` WHERE `NO`='"+ogrNo+"'");
            rs.next();
            yke.jLabelOgrenciNumarasi.setText(ogrNo);
            yke.jTextFieldOgrenciAd.setText(rs.getString("AD"));
            yke.jTextFieldOgrenciSoyad.setText(rs.getString("SOYAD"));
            yke.jTextFieldOgrenciTelNumarasi.setText(rs.getString("TELNO"));
            yke.jTextFieldOgrenciTel1.setText(rs.getString("TEL1"));
            yke.jTextFieldOgrenciTel2.setText(rs.getString("TEL2"));
            yke.jTextFieldOgrenciAdres.setText(rs.getString("ADRES"));
            yke.jTextFieldGeldigiOkul.setText(rs.getString("GELDIGIOKUL"));
            yke.getjLabelyke().setText("Öğrenci Kaydı Düzenle");
            
            yke.dateChooserComboDogumTarihi.setVisible(false);
            yke.dateChooserComboKayitTarihi.setVisible(false);
            yke.getjLabeldogum().setVisible(false);
            yke.getjLabelkayit().setVisible(false);
            yke.jTextFieldAnneAd.setText(rs.getString("ANNEAD"));
            yke.jTextFieldAnneSoyad.setText(rs.getString("ANNESOYAD"));
            yke.jTextFieldAnneTel1.setText(rs.getString("ANNETEL1"));
            yke.jTextFieldAnneTel2.setText(rs.getString("ANNETEL2"));
            yke.jTextFieldAnneTelNo.setText(rs.getString("ANNETELNO"));
 
            yke.jTextFieldBabaAd.setText(rs.getString("BABAAD"));
            yke.jTextFieldBabaSoyad.setText(rs.getString("BABASOYAD"));
            yke.jTextFieldBabaTelNo.setText(rs.getString("BABATELNO"));
            yke.jTextFieldBabaTel1.setText(rs.getString("BABATEL1"));
            yke.jTextFieldBabaTel2.setText(rs.getString("BABATEL2"));
            yke.jComboBoxGrubuOgrenciDurumu.setSelectedItem(rs.getString("OGRENCIDURUMU"));
            stmt = conn.createStatement();
            rs=stmt.executeQuery("SELECT * FROM `OGRENCILER` WHERE `NO`='"+ogrNo+"'");
            yke.jComboBoxGrubu1.setSelectedItem(rs.getString("GRUP1"));
            yke.jComboBoxGrubu2.setSelectedItem(rs.getString("GRUP2"));
            yke.jComboBoxSinifi1.setSelectedItem(rs.getString("SINIF1"));
            yke.jComboBoxSinifi2.setSelectedItem(rs.getString("SINIF2"));
            close();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
        
    }

    void updateOgrenciDurumu(String ogrNo, String OgrDurumu) {
        try {
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE `OGRENCILER` SET `OGRENCIDURUMU`='"+OgrDurumu+"' WHERE `NO`='"+ogrNo+"'");
            close();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
    }

    boolean ekleSinav(String ad, String soruSayisi, String tarih, Object grup) {
        try {
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            rs=stmt.executeQuery("SELECT COUNT(*) FROM `IMTAHANLAR` WHERE `AD`='"+ad+"'");
            rs.next();
            if(rs.getInt(1)>0)
            {   
                close();
                return false;
            }
            else
            {
                stmt = conn.createStatement();
                stmt.executeUpdate("INSERT INTO `IMTAHANLAR` VALUES('"+ad+"','"+soruSayisi+"','"+tarih+"','"+grup+"')");
                String query="CREATE TABLE `"+ad+"` (`NO` TEXT,`AD` TEXT,`SOYAD` TEXT,`GRUP1` TEXT,`SINIF1` TEXT,`GRUP2` TEXT,`SINIF2` TEXT,`DOGRU` TEXT,`YANLIS` TEXT,`PUAN` TEXT)";
                stmt = conn.createStatement();
                stmt.executeUpdate(query);
                close();
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
        close();
        return false;
    }

    public void sinavaGirecekleriEkle(int[] selection, String[][] matrix, String dbName) {
        try {  
            qosul(getCurrentYear());
            if(selection[0]==-1 )
            {
                int i,j;
                for(i=0;i<matrix.length;i++)
                {
                    System.out.println(matrix[i][0]);
                    if(!(matrix[i][0]==(null)))
                    {
                        stmt = conn.createStatement();
                        rs=stmt.executeQuery("SELECT count(*) FROM `"+dbName+"` WHERE `NO`='"+matrix[i][0]+"'");
                        if(rs.getInt(1)<=0)
                        {
                            String query="INSERT INTO `"+dbName+"` VALUES(";
                            for(j=0;j<matrix[i].length-1;j++)
                            {
                                query=query+"'"+matrix[i][j]+"',";
                            }
                            query=query+"'"+matrix[i][j]+"','0','0','0')";
                            stmt = conn.createStatement();
                            stmt.executeUpdate(query);
                        }
                    }    
                }
            }
            else
            {
                int j;
                for(int i:selection)
                {
                    System.out.println(matrix[i][0]);
                    
                    if(!(matrix[i][0]==(null)))
                    {
                        stmt = conn.createStatement();
                        rs=stmt.executeQuery("SELECT count(*) FROM `"+dbName+"` WHERE `NO`='"+matrix[i][0]+"'");
                        if(rs.getInt(1)<=0)
                        {
                            String query="INSERT INTO `"+dbName+"` VALUES(";
                            for(j=0;j<matrix[i].length-1;j++)
                            {
                                query=query+"'"+matrix[i][j]+"',";
                            }
                            query=query+"'"+matrix[i][j]+"','0','0','0')";
                            stmt = conn.createStatement();
                            stmt.executeUpdate(query);
                        }
                    }
                }
            }           
            close();            
        }   catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
        close();
    }

    public void sinavaGirecekleriSil(int[] selection, String[][] matrix, String dbName) {
        try{
        qosul(getCurrentYear());
        if(selection[0]==-1)
            {
                int i,j;
                for(i=0;i<matrix.length;i++)
                {
                    stmt = conn.createStatement();
                    rs=stmt.executeQuery("SELECT count(*) FROM `"+dbName+"` WHERE `NO`='"+matrix[i][0]+"'");
                    if(rs.getInt(1)>0)
                    {
                        stmt = conn.createStatement();
                        stmt.executeUpdate("DELETE FROM `"+dbName+"` WHERE `NO`='"+matrix[i][0]+"'");
                    }
                }
            }
            else
            {
                for(int i:selection)
                {
                    stmt = conn.createStatement();
                    rs=stmt.executeQuery("SELECT count(*) FROM `"+dbName+"` WHERE `NO`='"+matrix[i][0]+"'");
                    if(rs.getInt(1)>0)
                    {
                        stmt = conn.createStatement();
                        stmt.executeUpdate("DELETE FROM `"+dbName+"` WHERE `NO`='"+matrix[i][0]+"'");
                    }   
                }
            }
        }catch(SQLException ex){
        }finally{
            close();
        }
        close();            
        
        
    }

    void puanEkle(String no, String dogru, String yanlis, String puan,String dbName) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `"+dbName+"` SET `DOGRU`='"+dogru+"',`YANLIS`='"+yanlis+"',`PUAN`='"+puan+"' WHERE `NO`='"+no+"'");
            close();
            
        } catch(Exception ex){
            
        } finally{
            close();
        }
    }

    public boolean sinifEkle(String sinif, String grup) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT count(*) FROM `SINIFLAR` WHERE `AD`='"+sinif+"'");
            rs.next();
            if(rs.getInt(1)<=0)
            {
                stmt=conn.createStatement();
                stmt.executeUpdate("INSERT INTO `SINIFLAR` VALUES('"+sinif+"','"+grup+"')");
                close();
                return true;
            }
            close();
            return false;
        } catch(Exception ex){
        }finally{
            close();
        }
        
        close();
        return false;       
    }

    public boolean devamsizlikEkle(String no, String gun, String ad, String soyad, String grup1, String sinif1, String grup2, String sinif2,String tarih) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT COUNT(*) FROM `DEVAMSIZLIKLAR` WHERE `NO`='"+no+"'");
            System.out.println("gf");
            rs.next();
            if(rs.getInt(1)>0)
            {
                System.out.println("10");
                stmt=conn.createStatement();
                rs=stmt.executeQuery("SELECT `DEVAMSIZLIKSAYISI` FROM `DEVAMSIZLIKLAR` WHERE `NO`='"+no+"'");
                rs.next();        
                System.out.println("11");
                Double i=Double.parseDouble(rs.getString(1))+Double.parseDouble(gun);
                String yeniDevamsizlikgun= String.valueOf(i);
                stmt=conn.createStatement();
                stmt.executeUpdate("UPDATE `DEVAMSIZLIKLAR` SET `DEVAMSIZLIKSAYISI`='"+yeniDevamsizlikgun+"' WHERE `NO`='"+no+"'");
                stmt=conn.createStatement();
                System.out.println("12");
                stmt.executeUpdate("INSERT INTO `noDevamsizlik"+no+"` VALUES('"+no+"','"+tarih+"','"+gun+"')");
                System.out.println("asd");

                close();
                return true;
            }
            else{
                System.out.println("0");
                stmt=conn.createStatement();
                stmt.executeUpdate("INSERT INTO `DEVAMSIZLIKLAR` VALUES('"+no+"','"+gun+"','')");
                System.out.println("1");
                stmt=conn.createStatement();
                stmt.executeUpdate("CREATE TABLE `noDevamsizlik"+no+"` (`NO` TEXT,`TARIH` TEXT,`DEVAMSIZLIK` TEXT)");
                System.out.println("2");
                stmt=conn.createStatement();
                stmt.executeUpdate("INSERT INTO `noDevamsizlik"+no+"` VALUES('"+no+"','"+tarih+"','"+gun+"')");
            System.out.println("asd2");

                
                close();
                return true;
            }
        } catch(Exception ex){
            
        }finally {
            close();
        }
        close();
        return false;
    }

    boolean devamsizlikSil(String no, String gun, String tarih) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("DELETE FROM `noDevamsizlik"+no+"` WHERE `TARIH`='"+tarih+"'");
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT COUNT(*) FROM `noDevamsizlik"+no+"`");
            rs.next();
            if(rs.getInt(1)<=0)
            {
                close();
                this.silNoDevamsizlik(no);
                return true;
            }
            else{
                stmt=conn.createStatement();
                rs=stmt.executeQuery("SELECT `DEVAMSIZLIKSAYISI` FROM `DEVAMSIZLIKLAR` WHERE `NO`='"+no+"'");
                rs.next();
                String yeniDevamsizlik=String.valueOf(Double.parseDouble(rs.getString(1))-Double.parseDouble(gun));
                stmt=conn.createStatement();
                stmt.executeUpdate("UPDATE `DEVAMSIZLIKLAR` SET `DEVAMSIZLIKSAYISI`='"+yeniDevamsizlik+"' WHERE `NO`='"+no+"'");
                close();
                return true;
            }
            
        }catch(Exception ex)
        {
            close();
            return false;
        }finally{
            close();
        }
    }

    boolean silNoDevamsizlik(String no) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("DROP TABLE `noDevamsizlik"+no+"`");
            stmt=conn.createStatement();
            stmt.executeUpdate("DELETE FROM `DEVAMSIZLIKLAR` WHERE `NO`='"+no+"'");
            close();
            return true;
        }catch(Exception ex)
        {
            close();
            return false;
        }finally{
            close();
        }
    }

    boolean devamsizligiVarmi(String no) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT COUNT(*) FROM `DEVAMSIZLIKLAR` WHERE `NO`='"+no+"'");
            rs.next();
            if(rs.getInt(1)>0)
            {
                close();
                return true;
            }
            else
            {
                close();
                return false;
            }
            
        }catch(Exception ex){
            close();
            return false;
        }
        finally{
            close();
        }
    }

    boolean ogrenciSil(String no) {
        try{
            if(devamsizligiVarmi(no))
            {
                silNoDevamsizlik(no);
            }
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("DELETE FROM `OGRENCILER` WHERE `NO`='"+no+"'");
            String[] basliq={"AD","TARIH","GRUP","SORUSAYISI"};
            String [][] imtahanlar = getTable(basliq,"IMTAHANLAR");
            qosul(getCurrentYear());
            for(int i=0;imtahanlar!=null && i<imtahanlar.length;i++)
            {
                stmt=conn.createStatement();
                rs=stmt.executeQuery("SELECT COUNT(*) FROM `"+imtahanlar[i][0]+"` WHERE `NO`='"+no+"'");
                if(rs.getInt(1)>0)
                {                
                    stmt=conn.createStatement();
                    stmt.executeUpdate("DELETE FROM `"+imtahanlar[i][0]+"` WHERE `NO`='"+no+"'");
                }
            }
            stmt=conn.createStatement();
            stmt.executeUpdate("DELETE FROM `TAKSITLER` WHERE `NO`='"+no+"'");
            return true;
        }   catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
            return false;

        }
    }

    void personelEkle(PersonelKaydi pk,boolean duzenle) {
        try{
            qosul(getCurrentYear());
            System.out.println("fd");
            if(!duzenle)
            {
                stmt=conn.createStatement();
                stmt.executeUpdate("INSERT INTO `PERSONELLER` VALUES('"+pk.getjLabelPersonelNo().getText()+"','','','','','','','"+pk.getDateChooserComboDogumTarihi().getText()+"','"+pk.getDateChooserComboIseBasladigiTarih().getText()+"','','','','')");
            }
            System.out.println("asd");
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `PERSONELLER` SET "+
                    "`AD`='"+pk.getjTextFieldOgrenciAd().getText()+"',"+
                    "`SOYAD`='"+pk.getjTextFieldOgrenciSoyad().getText()+"',"+
                    "`TELNO`='"+pk.getjTextFieldOgrenciTelNumarasi().getText()+"',"+
                    "`TEL1`='"+pk.getjTextFieldOgrenciTel1().getText()+"',"+
                    "`TEL2`='"+pk.getjTextFieldOgrenciTel2().getText()+"',"+
                    "`ADRES`='"+pk.getjTextFieldOgrenciAdres().getText()+"',"+
                    "`BITIRDIGIOKUL`='"+pk.getjTextFieldBitirdigiOkul().getText()+"',"+
                    "`BRANS`='"+pk.getjTextFieldBrans().getText()+"',"+
                    "`MAAS`='"+pk.getjTextFieldMaas().getText()+"' "+
                    "WHERE `NO`='"+pk.getjLabelPersonelNo().getText()+"'");
            String ogret;
            if(pk.getjCheckBoxOgretmenmi().isSelected())
            {
                ogret="Evet";
            }
            else{
                ogret="Hayır";
            }
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `PERSONELLER` SET `OGRETMENMI`='"+ogret+"' WHERE `NO`='"+pk.getjLabelPersonelNo().getText()+"'");
            close();
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
    }

    void artirPersonelNumarasi(String numara) {
        try {
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            stmt.executeUpdate("UPDATE `PROGRAM` SET `PERNO`=+'"+String.valueOf((Integer.parseInt(numara)+1))+"' WHERE 1");
            close();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
    }

    String getPerNumarasiString() {
        try {
            qosul(getCurrentYear());
            stmt = conn.createStatement();
                rs= stmt.executeQuery("SELECT `PERNO` FROM `PROGRAM`");                
            if(rs.next())
            {
                String a=rs.getString("PERNO");
                close();
                return a;
            }
            close();
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            close();
        }
        close();
        return null;
    }

    void setPersonelKaydi(PersonelKaydi yke, String no) {
        try{
            qosul(getCurrentYear());
            stmt = conn.createStatement();
            rs=stmt.executeQuery("SELECT * FROM `PERSONELLER` WHERE `NO`='"+no+"'");
            rs.next();
            if(rs.getString("OGRETMENMI").equals("Evet"))
            {
                yke.getjCheckBoxOgretmenmi().setSelected(true);
            }
            else
            {
                yke.getjCheckBoxOgretmenmi().setSelected(false);
            }
            yke.jLabelPersonelNo.setText(no);
            yke.getjTextFieldOgrenciAd().setText(rs.getString("AD"));
            yke.getjTextFieldOgrenciAdres().setText(rs.getString("ADRES"));
            yke.getjTextFieldOgrenciSoyad().setText(rs.getString("SOYAD"));
            yke.getjTextFieldBitirdigiOkul().setText(rs.getString("BITIRDIGIOKUL"));
            yke.getjTextFieldBrans().setText(rs.getString("BRANS"));
            yke.getjTextFieldMaas().setText(rs.getString("MAAS"));
            yke.getjTextFieldOgrenciTelNumarasi().setText(rs.getString("TELNO"));
            yke.getjTextFieldOgrenciTel1().setText(rs.getString("TEL1"));
            yke.getjTextFieldOgrenciTel2().setText(rs.getString("TEL2"));
            yke.dateChooserComboDogumTarihi.setVisible(false);
            yke.dateChooserComboIseBasladigiTarih.setVisible(false);
            yke.jLabeldogum.setVisible(false);
            yke.jLabeltarih.setVisible(false);
            close();
        }catch(Exception ex)
        {
            close();
        }
    
        
    }
    
    public void giderEkle(String giderAdi,String giderTipi,String tarih,String tutar,String kime){
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("INSERT INTO `GIDERLER` VALUES('"+giderAdi+"','"+giderTipi+"','"+tarih+"','"+tutar+"','"+kime+"')");
            close();
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        close();
    }
    
    public String getMaas(String no)
    {
        try{
            
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT `MAAS` FROM `PERSONELLER` WHERE `NO`='"+no+"'");
            rs.next();
            String maas=rs.getString(1);
            close();
            return maas;
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        close();
        return null;
    }

    String[][] getSinifOgrencileri(String grup,String sinif) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT `NO`,`AD`,`SOYAD`,`GRUP1`,`GRUP2` FROM `OGRENCILER` WHERE (`SINIF1`='"+sinif+"') OR (`SINIF2`='"+sinif+"')");
        String[][] matrix;
        ArrayList matr=new ArrayList();
        while(rs.next())
        {
            matr.add(rs.getString(1));
            matr.add(rs.getString(2));
            matr.add(rs.getString(2));
            matr.add(rs.getString(4));
            matr.add(rs.getString(5));
        }
        matrix=new String[matr.size()/5][5];
        int k=0;
        for(int i=0;i<matr.size()/5;i++)
        {
            for(int j=0;j<5;j++)
            {
                matrix[i][j]=(String)matr.get(k);
                k++;
            }
        }
        close();
        return matrix;
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        String[][] matrix=new String[1][5];
        matrix[0][0]="";
        matrix[0][1]="";
        matrix[0][2]="";
        matrix[0][3]="";
        matrix[0][4]="";
        close();        
        return matrix;
    }

    String[][] getSinifGrupOgrenci(String no) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT `SINIF1`,`GRUP1`,`SINIF2`,`GRUP2` FROM `OGRENCILER` WHERE `NO`='"+no+"'");
            rs.next();
            String[][] mat=new String[2][2];
            mat[0][0]=rs.getString(1);
            mat[0][1]=rs.getString(2);
            mat[1][0]=rs.getString(3);
            mat[1][1]=rs.getString(4);
            close();
            return mat;
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        close();
        return null;
    }

    void setSinifOgrenci(String sinifGrupOgrenci, int matrixI, String no) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            if(matrixI==0)
            {
                stmt.executeUpdate("UPDATE `OGRENCILER` SET `SINIF1`='"+sinifGrupOgrenci+"' WHERE `NO`='"+no+"'");
            }
            else
            {
                stmt.executeUpdate("UPDATE `OGRENCILER` SET `SINIF2`='"+sinifGrupOgrenci+"' WHERE `NO`='"+no+"'");                
            }
            close();
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        close();
    }

    void sinifOgrenciSil(String no, String sinif) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `OGRENCILER` SET `SINIF1`='' WHERE (`NO`='"+no+"' AND `SINIF1`='"+sinif+"')");
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `OGRENCILER` SET `SINIF2`='' WHERE (`NO`='"+no+"' AND `SINIF2`='"+sinif+"')");
            close();
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        close();        
    }

    boolean silSinif(String sinif, String grup) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("DELETE FROM `SINIFLAR` WHERE (`AD`='"+sinif+"' AND `GRUP`='"+grup+"')");
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `OGRENCILER` SET `SINIF1`='' WHERE `SINIF1`='"+sinif+"'");
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `OGRENCILER` SET `SINIF2`='' WHERE `SINIF2`='"+sinif+"'");
            close();
            return true;
        }catch(Exception ex)
        {
            close();
            return false;
        }finally{
            close();
        }
    }

    public void gelirEkle(String no,String tarih,String tutar)
    {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("INSERT INTO `GELIRLER` VALUES('"+no+"','"+tarih+"','"+tutar+"')");
            close();
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        
    }

    boolean taksitleriYukle(TaksitliSatis takSat) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("INSERT INTO `TAKSITLER` VALUES('"+takSat.jLabelNo2.getText()+"','"+takSat.jTextFieldTutar.getText()+"','"+takSat.jTextFieldPesinat.getText()+"','','','','','','','','','','','','','','','','','HAYIR','HAYIR','HAYIR','HAYIR','HAYIR','HAYIR','HAYIR','HAYIR','"+String.valueOf(takSat.jComboBox3.getSelectedItem())+"')");
            close();
                taksitEkle(takSat.jLabelNo2.getText(),takSat.jTextField1Taksit.getText(),takSat.dateChooserCombo1.getText(),"1TAKSITTARIH","1TAKSITTUTAR",String.valueOf(takSat.jComboBox3.getSelectedItem()));
            
                taksitEkle(takSat.jLabelNo2.getText(),takSat.jTextField2Taksit.getText(),takSat.dateChooserCombo2.getText(),"2TAKSITTARIH","2TAKSITTUTAR",String.valueOf(takSat.jComboBox3.getSelectedItem()));
            
                taksitEkle(takSat.jLabelNo2.getText(),takSat.jTextField3Taksit.getText(),takSat.dateChooserCombo3.getText(),"3TAKSITTARIH","3TAKSITTUTAR",String.valueOf(takSat.jComboBox3.getSelectedItem()));
            
                taksitEkle(takSat.jLabelNo2.getText(),takSat.jTextField4Taksit.getText(),takSat.dateChooserCombo4.getText(),"4TAKSITTARIH","4TAKSITTUTAR",String.valueOf(takSat.jComboBox3.getSelectedItem()));
            
                taksitEkle(takSat.jLabelNo2.getText(),takSat.jTextField5Taksit.getText(),takSat.dateChooserCombo5.getText(),"5TAKSITTARIH","5TAKSITTUTAR",String.valueOf(takSat.jComboBox3.getSelectedItem()));
            
                taksitEkle(takSat.jLabelNo2.getText(),takSat.jTextField6Taksit.getText(),takSat.dateChooserCombo6.getText(),"6TAKSITTARIH","6TAKSITTUTAR",String.valueOf(takSat.jComboBox3.getSelectedItem()));
            
                taksitEkle(takSat.jLabelNo2.getText(),takSat.jTextField7Taksit.getText(),takSat.dateChooserCombo7.getText(),"7TAKSITTARIH","7TAKSITTUTAR",String.valueOf(takSat.jComboBox3.getSelectedItem()));
            
                taksitEkle(takSat.jLabelNo2.getText(),takSat.jTextField8Taksit.getText(),takSat.dateChooserCombo8.getText(),"8TAKSITTARIH","8TAKSITTUTAR",String.valueOf(takSat.jComboBox3.getSelectedItem()));
                if(Integer.parseInt(takSat.jTextFieldPesinat.getText())>0) {
                    gelirEkle(takSat.jLabelNo2.getText(), java.time.LocalDate.now().getMonthValue()+"/"+java.time.LocalDate.now().getDayOfMonth()+"/"+java.time.LocalDate.now().getYear()%2000, takSat.jTextFieldPesinat.getText());
                }
            
                return true;
        }catch(Exception ex)
        {
            close();
            return false;
        }finally{
            close();
        }
        
    }

    private void taksitEkle(String no, String tutar, String tarih, String taksitTarihAdi, String taksitTutarAdi,String grup) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `TAKSITLER` SET `"+taksitTutarAdi+"`='"+tutar+"',`"+taksitTarihAdi+"`='"+tarih+"' WHERE (`NO`='"+no+"' AND `GRUP`='"+grup+"')");
            close();
        }catch(Exception ex){
            close();
        }finally{
            close();
        }
    }

    public boolean grupEkle(String grup) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("INSERT INTO `GRUPLAR` VALUES('"+grup+"')");
            close();
            return true;
        }catch(Exception ex){
            close();
            return false;
        }finally{
            close();
        }
    }

    public boolean grupSil(String grup) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("DELETE FROM `GRUPLAR` WHERE `AD`='"+grup+"'");
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `OGRENCILER` SET `GRUP1`='' WHERE `GRUP1`='"+grup+"'");
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `OGRENCILER` SET `GRUP2`='' WHERE `GRUP2`='"+grup+"'");
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `SINIFLAR` SET `GRUP`='' WHERE `GRUP`='"+grup+"'");
            stmt=conn.createStatement();
            stmt.executeUpdate("UPDATE `IMTAHANLAR` SET `GRUP`='' WHERE `GRUP`='"+grup+"'");
            close();
            return true;
        }catch(Exception ex){
            close();
            return false;
        }finally{
            close();
        }
        
    }

    String[][] getTaksitlerTable(String tip) {
        String [][] mat;
        try{
            qosul(getCurrentYear());
            if(tip.equals("tum"))
            {
                int count=0;
                for(int i=1;i<9;i++)
                {
                    stmt=conn.createStatement();
                    rs=stmt.executeQuery("SELECT COUNT(*) FROM `TAKSITLER` WHERE `"+i+"TAKSITTUTAR`!='0'");
                    rs.next();
                    count+=rs.getInt(1);
                }
                if(count>0)
                {
                    mat=new String[count][6];
                    int k=0;
                    for(int i=1;i<9;i++)
                    {
                        stmt=conn.createStatement();
                        rs=stmt.executeQuery("SELECT `NO`,`"+i+"TAKSITTUTAR`,`"+i+"TAKSITTARIH`,`GRUP` FROM `TAKSITLER` WHERE `"+i+"TAKSITTUTAR`!='0'");
                        while(rs.next())
                        {
                            mat[k][0]=rs.getString(1);
                            mat[k][4]=rs.getString(2);
                            mat[k][3]=rs.getString(3);
                            mat[k][5]=rs.getString(4);
                            k++;
                        }
                    }
                    for(int i=0;i<count;i++)
                    {
                        stmt=conn.createStatement();
                        rs=stmt.executeQuery("SELECT `AD`,`SOYAD` FROM `OGRENCILER` WHERE `NO`='"+mat[i][0]+"'");
                        rs.next();
                        mat[i][1]=rs.getString(1);
                        mat[i][2]=rs.getString(2);
                    }
                    close();
                    return mat;
                    
                }
                else{
                    close();
                    mat=new String[1][6];
                    for(int i=0;i<5;i++)
                    {
                        mat[0][i]="";
                    }
                    return mat;
                }
            }
            else if(tip.equals("odenmis")){
                int count=0;
                for(int i=1;i<9;i++)
                {
                    stmt=conn.createStatement();
                    rs=stmt.executeQuery("SELECT COUNT(*) FROM `TAKSITLER` WHERE (`"+i+"TAKSITTUTAR`!='0' AND `"+i+"TAKSITODENDIMI`='EVET')");
                    rs.next();
                    count+=rs.getInt(1);
                }
                if(count>0)
                {
                    mat=new String[count][6];
                    int k=0;
                    for(int i=1;i<9;i++)
                    {
                        stmt=conn.createStatement();
                        rs=stmt.executeQuery("SELECT `NO`,`"+i+"TAKSITTUTAR`,`"+i+"TAKSITTARIH`,`GRUP` FROM `TAKSITLER` WHERE (`"+i+"TAKSITTUTAR`!='0' AND `"+i+"TAKSITODENDIMI`='EVET')");
                        while(rs.next())
                        {
                            mat[k][0]=rs.getString(1);
                            mat[k][4]=rs.getString(2);
                            mat[k][3]=rs.getString(3);
                            mat[k][5]=rs.getString(4);
                            k++;
                        }
                    }
                    for(int i=0;i<count;i++)
                    {
                        stmt=conn.createStatement();
                        rs=stmt.executeQuery("SELECT `AD`,`SOYAD` FROM `OGRENCILER` WHERE `NO`='"+mat[i][0]+"'");
                        rs.next();
                        mat[i][1]=rs.getString(1);
                        mat[i][2]=rs.getString(2);
                    }
                    close();
                    return mat;
                    
                }
                else{
                    close();
                    mat=new String[1][6];
                    for(int i=0;i<5;i++)
                    {
                        mat[0][i]="";
                    }
                    return mat;
                }
            }
            else
            {
                int count=0;
                for(int i=1;i<9;i++)
                {
                    stmt=conn.createStatement();
                    rs=stmt.executeQuery("SELECT COUNT(*) FROM `TAKSITLER` WHERE (`"+i+"TAKSITTUTAR`!='0' AND `"+i+"TAKSITODENDIMI`='HAYIR')");
                    rs.next();
                    count+=rs.getInt(1);
                }
                if(count>0)
                {
                    mat=new String[count][7];
                    int k=0;
                    for(int i=1;i<9;i++)
                    {
                        stmt=conn.createStatement();
                        rs=stmt.executeQuery("SELECT `NO`,`"+i+"TAKSITTUTAR`,`"+i+"TAKSITTARIH`,`GRUP` FROM `TAKSITLER` WHERE (`"+i+"TAKSITTUTAR`!='0' AND `"+i+"TAKSITODENDIMI`='HAYIR')");
                        while(rs.next())
                        {
                            mat[k][0]=rs.getString(1);
                            mat[k][4]=rs.getString(2);
                            mat[k][3]=rs.getString(3);
                            mat[k][6]=rs.getString(4);
                            k++;
                        }
                    }
                    for(int i=0;i<count;i++)
                    {
                        stmt=conn.createStatement();
                        rs=stmt.executeQuery("SELECT `AD`,`SOYAD` FROM `OGRENCILER` WHERE `NO`='"+mat[i][0]+"'");
                        rs.next();
                        mat[i][1]=rs.getString(1);
                        mat[i][2]=rs.getString(2);
                        mat[i][5]=gecikmeHesapla(mat[i][3]);
                    }
                    close();
                    return mat;
                    
                }
                else{
                    close();
                    mat=new String[1][6];
                    for(int i=0;i<6;i++)
                    {
                        mat[0][i]="";
                    }
                    return mat;
                }
            }
        }catch(Exception ex){
             close();
        }finally{
             close();
        }
        return null;    
    }
    
    public String gecikmeHesapla(String tarih){
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date today = new Date();
            Date firstDate =  sdf.parse((Calendar.getInstance().get(Calendar.MONTH)+1)+"/"+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"/"+(Calendar.getInstance().get(Calendar.YEAR)%2000));
            System.out.println(((Calendar.getInstance().get(Calendar.MONTH)+1)+"/"+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"/"+(Calendar.getInstance().get(Calendar.YEAR)%2000)));
            Date secondDate =  sdf.parse(tarih);
            System.out.println(tarih);
            System.out.println(secondDate);
            long diffInMillies = secondDate.getTime() - firstDate.getTime();
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            return String.valueOf(diff);
        } catch (ParseException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }

    boolean taksitOde(String no, String tarih, String tutar,String grup) {
        try{
            qosul(getCurrentYear());
            for(int i=1;i<9;i++)
            {
                System.out.println("asd");
                stmt=conn.createStatement();
                stmt.executeUpdate("UPDATE `TAKSITLER` SET `"+i+"TAKSITODENDIMI`='EVET' WHERE (`NO`='"+no+"' AND `"+i+"TAKSITTARIH`='"+tarih+"' AND `"+i+"TAKSITTUTAR`='"+tutar+"' AND `GRUP`='"+grup+"')");
                stmt=conn.createStatement();
                rs=stmt.executeQuery("SELECT `AD`,`SOYAD` FROM `OGRENCILER` WHERE `NO`='"+no+"'");
                rs.next();
                String ad=rs.getString(1);
                String soyad=rs.getString(2);
                stmt=conn.createStatement();
                rs=stmt.executeQuery("SELECT COUNT(*) FROM `GELIRLER` WHERE (`NO`='"+no+"'AND `TARIH`='"+tarih+"' AND `TUTAR`='"+tutar+"')");
                rs.next();
                if(rs.getInt(1)<=0)
                {
                    stmt=conn.createStatement();
                    stmt.executeUpdate("INSERT INTO `GELIRLER` VALUES('"+no+"','"+tarih+"','"+tutar+"')");
                }
            }
            close();
            return true;
        }catch(SQLException ex){
            close();
            return false;
        }finally{
            close();
        }
    }

    String[][] getTableGelir() {
        String [][]mat=new String[1][4];
        int gunluk=0;
        int aylik=0;
        int yillik=0;
        int toplam=0;
        String ay=String.valueOf(java.time.LocalDate.now().getMonthValue());
        String yil=String.valueOf(java.time.LocalDate.now().getYear()%2000);
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT `TARIH`,`TUTAR` FROM `GELIRLER`");
            while(rs.next()){
                if(gecikmeHesapla(rs.getString(1)).equals("0"))
                {
                    gunluk+=Double.parseDouble(rs.getString(2));
                }
                String [] parts=rs.getString(1).split("/");
                if(parts[0].equals(ay) && parts[2].equals(yil))
                {
                    aylik+=Double.parseDouble(rs.getString(2));
                }
                if(parts[2].equals(yil))
                {
                    yillik+=Double.parseDouble(rs.getString(2));
                }
                toplam+=Double.parseDouble(rs.getString(2));
            }
            mat[0][0]=String.valueOf(gunluk);
            mat[0][1]=String.valueOf(aylik);
            mat[0][2]=String.valueOf(yillik);
            mat[0][3]=String.valueOf(toplam);
            close();
            return mat;
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        mat[0][0]="0";
        mat[0][1]="0"; 
        mat[0][2]="0"; 
        mat[0][3]="0"; 
        close();
        return mat;
    }

    public String [][] getTableGelirHamsi()
    {
        String [][] gelirler=null;
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT COUNT(*) FROM `GELIRLER`");
            rs.next();
            int k=rs.getInt(1);
            if(k>0)
            {
                gelirler=new String[k][5];
            }
            if(k>0)
            {

                stmt=conn.createStatement();
                rs=stmt.executeQuery("SELECT `NO`,`TARIH`,`TUTAR` FROM `GELIRLER`");
                int i=0;
                while(rs.next())
                {
                    gelirler[i][0]=rs.getString(1);
                    gelirler[i][3]=rs.getString(2);
                    gelirler[i][4]=rs.getString(3);
                    i++;
                }
                System.out.println("asd");

                for(i=0;i<gelirler.length;i++)
                {
                    stmt=conn.createStatement();
                    rs=stmt.executeQuery("SELECT `AD`,`SOYAD` FROM `OGRENCILER` WHERE `NO`='"+gelirler[i][0]+"' ");
                    rs.next();
                    gelirler[i][1]=rs.getString(1);
                    gelirler[i][2]=rs.getString(2);
                }
                close();
                return gelirler;
            }
            close();
            return gelirler;
        }catch(Exception ex)
        {
            close();
            return gelirler;
        }
    }
    
    String[][] getTableGider() {
        String [][]mat=new String[1][4];
        int gunluk=0;
        int aylik=0;
        int yillik=0;
        int toplam=0;
        String ay=String.valueOf(java.time.LocalDate.now().getMonthValue());
        String yil=String.valueOf(java.time.LocalDate.now().getYear()%2000);
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT `TARIH`,`TUTAR` FROM `GIDERLER`");
            while(rs.next()){
                if(gecikmeHesapla(rs.getString(1)).equals("0"))
                {
                    gunluk+=Double.parseDouble(rs.getString(2));
                }
                String [] parts=rs.getString(1).split("/");
                if(parts[0].equals(ay) && parts[2].equals(yil))
                {
                    aylik+=Double.parseDouble(rs.getString(2));
                }
                if(parts[2].equals(yil))
                {
                    yillik+=Double.parseDouble(rs.getString(2));
                }
                toplam+=Double.parseDouble(rs.getString(2));
            }
            mat[0][0]=String.valueOf(gunluk);
            mat[0][1]=String.valueOf(aylik);
            mat[0][2]=String.valueOf(yillik);
            mat[0][3]=String.valueOf(toplam);
            close();
            return mat;
        }catch(Exception ex)
        {
            mat[0][0]="0";
            mat[0][1]="0"; 
            mat[0][2]="0"; 
            mat[0][3]="0"; 
            close();
            return mat;
        }finally{
            close();
        }
    }

    
    public void fileYarat(String fileAdi)
    {
        PrintWriter writer = null;
        try {
            String home = System.getProperty("user.home");
            String out = home+"/Filelar/"+fileAdi;
            new File(home+"/Filelar").mkdirs();
            File source = new File(out);
            writer = new PrintWriter(source);
            writer.print("");
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }
    }

    private void fileEditordaAc(String fileAdi) {
        try {
            String home = System.getProperty("user.home");
            String out = home+"/Filelar/"+fileAdi;
            File source = new File(out);
            java.awt.Desktop.getDesktop().open(source);
        } catch (IOException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    void ogrenciOzelRaporla(String no, boolean taksit, boolean devamsizlik, boolean sinavNotlari) {
        fileYarat(no+" Ogrenci Ozel Raporlama");
        if(devamsizlik)
        {
            dosyayaMatrixYaz(no,"Devamsizlik",no+" Ogrenci Ozel Raporlama");            
        }
        if(sinavNotlari)
        {
            dosyayaMatrixYaz(no,"SinavNotlari",no+" Ogrenci Ozel Raporlama");                        
        }
        if(taksit)
        {
            dosyayaMatrixYaz(no,"Taksit",no+" Ogrenci Ozel Raporlama");                        
        }
        fileEditordaAc(no+" Ogrenci Ozel Raporlama");        
    }
    
    boolean muhasebeRaporlari(boolean gelirler, boolean giderler) 
    {
        fileYarat("Muhasebe Raporlari");
       
        if(gelirler) {
            dosyayaMatrixYaz(null,"gelirler","Muhasebe Raporlari");
        }
        if(giderler)
        {
            dosyayaMatrixYaz(null,"giderler","Muhasebe Raporlari");
        }
        
        fileEditordaAc("Muhasebe Raporlari");  
        return true;
    }

    void sinavSonuclariYazdir(String sinavIsmi) {
        fileYarat(sinavIsmi+" Sinav Sonuclari");
        dosyayaMatrixYaz(null,sinavIsmi,sinavIsmi+" Sinav Sonuclari");
        fileEditordaAc(sinavIsmi+" Sinav Sonuclari");        
    }

    private void dosyayaMatrixYaz(String no, String time, String fileAdi) {
        PrintWriter writer=null;
        try {
            String [][] mat;
            mat=getMatrix(no,time);
            String home = System.getProperty("user.home");
            String out = home+"/Filelar/"+fileAdi;
            File source = new File(out);
            if(mat!=null)
            {
                OutputStream os;
                os = Files.newOutputStream(Paths.get(out),APPEND);
                writer=new PrintWriter(os);
                
                if(no!=null)
                {
                    /* Ogrenci */
                    if(time.equals("SinavNotlari"))
                    {
                        writer.printf("%80s","Sınav Notları");
                        writer.println("");
                        writer.println("");
                        writer.println("");
                        String []basliq={"İmtahan adi","Dogru","Yanlis","Puan"};

                        for(String i: basliq)
                        {
                            writer.printf("%12s", i);
                        }
                        writer.println("");
                        for(String j[]:mat)
                        {
                            for(String i:j)
                            {
                                if(i!=null)
                                    writer.printf("%12s", i);
                                else
                                    writer.printf("%12s","");
                            }
                            writer.println("");
                        }
                        writer.println("");
                        writer.println("");
                        writer.println("");                        
                    }
                    else if(time.equals("Devamsizlik") && devamsizligiVarmi(no))
                    {
                        writer.printf("%80s","Devamsızlık Listesi");
                        writer.println("");
                        writer.println("");
                        writer.println("");                        
                        String [] basliq = {"NO","TARIH","DEVAMSIZLIK"} ;

                        for(String i: basliq)
                        {
                            writer.printf("%16s", i);
                        }
                        writer.println("");
                        for(String j[]:mat)
                        {
                            for(String i:j)
                            {
                                if(i!=null)
                                    writer.printf("%16s", i);
                                else
                                    writer.printf("%16s","");
                            }
                            writer.println("");
                        }
                        writer.println("");
                        writer.println("");
                        writer.println("");                        
                    }
                    else if(time.equals("Taksit"))
                    {
                        writer.printf("%80s","Taksit Bilgileri");
                        writer.println("");
                        writer.println("");
                        writer.println("");  
                        writer.println("Tutar:   "+mat[0][0]);
                        writer.println("Pesinat: "+mat[0][1]);
                        writer.println();
                        writer.println();
                        
                        String [] basliq = {"TUTAR","TARIH","ÖDENDİMİ?"} ;

                        for(String i: basliq)
                        {
                            writer.printf("%12s", i);
                        }
                        writer.println("");
                        for(int j=1;j<mat.length;j++)
                        {
                            for(String i:mat[j])
                            {
                                if(i!=null)
                                    writer.printf("%12s", i);
                                else
                                    writer.printf("%12s","");
                            }
                            writer.println("");
                        }
                        writer.println("");
                        writer.println("");
                        writer.println("");           
                    }
                }
                else if(time.equals("giderler") || time.equals("gelirler") )   
                {
                    /* Muhasebe */
                    if(time.equals("gelirler"))
                    {
                        String [] basliqTum={"Günlük","Aylık","Yıllık","Toplam"};
                        String [][] tum=getTableGelir();
                        writer.printf("%80s","GELIRLER");
                        writer.println("");
                        writer.println("");
                        writer.println("");
                        for(String i: basliqTum)
                        {
                            writer.printf("%12s", i);
                        }
                        writer.println("");
                        

                        for(String i:tum[0])
                        {
                            writer.printf("%12s", i);
                        }
                        writer.println("");
                        writer.println("");
                        writer.println("");

                        String [] basliq={"NO","AD","SOYAD","TARIH","TUTAR"};
                        for(String i: basliq)
                        {
                            writer.printf("%16s", i);
                        }
                        writer.println("");
                        for(String j[]:mat)
                        {
                            for(String i:j)
                            {
                                if(i!=null)
                                    writer.printf("%16s", i);
                                else
                                    writer.printf("%16s","");
                            }
                            writer.println("");
                        }
                        writer.println("");
                        writer.println("");
                        writer.println("");
                        writer.println("");
                        
                    }
                    else{
                        writer.printf("%80s","GIDERLER");
                        writer.println("");
                        writer.println("");
                        writer.println("");

                        String [] basliqTum={"Günlük","Aylık","Yıllık","Toplam"};
                        String [][] tum=getTableGider();
                        for(String i: basliqTum)
                        {
                            writer.printf("%12s", i);
                        }
                        writer.println("");

                        for(String i:tum[0])
                        {
                            writer.printf("%12s", i);
                        }

                        writer.println("");
                        writer.println("");
                        writer.println("");

                        String [] basliq={"GIDERADI","GIDERTIPI","TARIH","TUTAR","KIME"};
                        for(String i: basliq)
                        {
                            writer.printf("%16s", i);
                        }
                        writer.println("");
                        for(String j[]:mat)
                        {
                            for(String i:j)
                            {
                                if(i!=null)
                                    writer.printf("%16s", i);
                                else
                                    writer.printf("%16s","");
                            }
                            writer.println("");
                        }
                        writer.println("");
                        writer.println("");
                        writer.println("");
                        writer.println("");
                        
                    }
                }
                else
                {
                    /* Sinav sonuclari */
                    writer.printf("%80s",fileAdi);
                    writer.println("");
                    writer.println("");
                    writer.println("");

                    String[] basliq={"NO","AD","SOYAD","GRUP1","SINIF1","GRUP2","SINIF2","DOGRU","YANLIS","PUAN"};
                    for(String i: basliq)
                    {
                        writer.printf("%12s", i);
                    }
                    writer.println("");
                    for(String j[]:mat)
                    {
                        for(String i:j)
                        {
                            if(i!=null)
                                writer.printf("%12s", i);
                            else
                                writer.printf("%12s","");
                        }
                        writer.println("");
                    }
                }
                writer.close();
            }
        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @SuppressWarnings("unchecked")
    private String[][] getMatrix(String no, String time) {
        String [][] mat;
        try{
            qosul(getCurrentYear());
            if(no!=null)
            {
                /* Ogrenci */
                if(time.equals("SinavNotlari"))
                {
                    String []basliq={"AD"};
                    String [][] imtahanlar = getTable(basliq,"IMTAHANLAR");
                    ArrayList imt=new ArrayList();
                    for(int i=0;i<imtahanlar.length;i++)
                    {
                        String [] puanlar=getPuan(no,imtahanlar[i][0]);
                        imt.add(imtahanlar[i][0]);
                        imt.add(puanlar[0]);
                        imt.add(puanlar[1]);
                        imt.add(puanlar[2]);
                    }
                    imtahanlar = new String[(imt.size())/4][4];
                    int k=0;
                    for(int i=0;i<imt.size()/4;i++)
                    {
                        for(int j=0;j<4;j++)
                        {
                            imtahanlar[i][j]=(String)imt.get(k);
                            k++;
                        }
                    }
                    return imtahanlar;
                }
                else if(time.equals("Devamsizlik") && devamsizligiVarmi(no))
                {
                    String [] basliq = {"NO","TARIH","DEVAMSIZLIK"} ;
                    String [][] devamsizliklar = getTable(basliq, "noDevamsizlik"+no) ;
                    return devamsizliklar ;
                }
                else if(time.equals("Taksit"))
                {
                    String [][] taksitler = getTaksit(no);
                    return taksitler;
                }
            }
            else if( time.equals("giderler") || time.equals("gelirler") )
            {
                /* Muhasebe */
                if(time.equals("gelirler"))   
                {
                    String [] basliq2={"NO","AD","SOYAD","TARIH","TUTAR"};
                    String[][] gelirler = getTableGelirHamsi();
                    return gelirler;
                }
                if(time.equals("giderler"))
                {
                    String [] basliq1={"GIDERADI","GIDERTIPI","TARIH","TUTAR","KIME"};
                    String [][] giderler = getTable(basliq1,"GIDERLER");
                    return giderler;
                }
            }
            else
            {
                
                /* Sinav sonuclari */
                String[] basliq={"NO","AD","SOYAD","GRUP1","SINIF1","GRUP2","SINIF2","DOGRU","YANLIS","PUAN"};
                String [][] sinavSonuclari=getTable(basliq, time);
                return sinavSonuclari;
                
            }
            
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        close();
        return null;     
    }

    private String[] getPuan(String no,String DB) {
        try{
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT `DOGRU`,`YANLIS`,`PUAN` FROM `"+DB+"` WHERE `NO`='"+no+"'");
            String []puanlar = new String[3];
            rs.next();
            puanlar[0]=rs.getString(1);
            puanlar[1]=rs.getString(2);
            puanlar[2]=rs.getString(3);
            close();
            return puanlar;
        }catch(Exception ex)
        {
            close();
        }finally{
            close();
        }
        close();
        return null;
    }

    private String[][] getTaksit(String no) {
        try{
            ArrayList ars=new ArrayList();
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT `TUTAR`,`PESINAT` FROM `TAKSITLER` WHERE `NO`='"+no+"'");
            if(rs.next())
            {
                int tutar=0;
                int pesinat=0;
                tutar+=Integer.parseInt(rs.getString(1));
                pesinat+=Integer.parseInt(rs.getString(2));
                while(rs.next())
                {                    
                    tutar+=Integer.parseInt(rs.getString(1));
                    pesinat+=Integer.parseInt(rs.getString(2));
                }
                ars.add(String.valueOf(tutar));
                ars.add(String.valueOf(pesinat));
                ars.add("");
                for(int i=1;i<9;i++)
                {
                    stmt=conn.createStatement();
                    rs=stmt.executeQuery("SELECT `"+i+"TAKSITTUTAR`,`"+i+"TAKSITTARIH`,`"+i+"TAKSITODENDIMI` FROM `TAKSITLER` WHERE (`"+i+"TAKSITTUTAR`!='0')");
                    while(rs.next())
                    {
                        ars.add(rs.getString(1));
                        ars.add(rs.getString(2));
                        ars.add(rs.getString(3));
                    }                    
                }
                String[][] taksitler=new String[ars.size()/3][3];
                int k=0;
                for(int i=0;i<ars.size()/3;i++)
                {
                    for(int j=0;j<3;j++)
                    {
                        taksitler[i][j]=(String)ars.get(k);
                        k++;
                    }
                }
                close();
                return taksitler;
            }
        }catch(Exception ex){
            close();
        }finally{
            close();
        }
        close();
        return null;
    }

    String[][] getTableDevamsizliklar() {
        String [][]mat=null;
        try {
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT COUNT(*) FROM DEVAMSIZLIKLAR");
            rs.next();
            int say=rs.getInt(1);
            int i=0;
            if(say>0)
            {
                mat=new String[say][8];                
                stmt=conn.createStatement();
                rs=stmt.executeQuery("SELECT `NO`,`DEVAMSIZLIKSAYISI` FROM `DEVAMSIZLIKLAR`");
                while(rs.next())
                {
                    mat[i][0]=rs.getString(1);
                    mat[i][3]=rs.getString(2);
                    i++;
                }
                for(i=0;i<say;i++)
                {
                    stmt=conn.createStatement();
                    rs=stmt.executeQuery("SELECT `AD`,`SOYAD`,`GRUP1`,`SINIF1`,`GRUP2`,`SINIF2` FROM `OGRENCILER` WHERE `NO`='"+mat[i][0]+"'");
                    rs.next();
                    mat[i][1]=rs.getString(1);
                    mat[i][2]=rs.getString(2);
                    mat[i][4]=rs.getString(3);
                    mat[i][5]=rs.getString(4);
                    mat[i][6]=rs.getString(5);
                    mat[i][7]=rs.getString(6);
                }
            }            
            close();
            return mat;
        } catch (SQLException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
            close();
            return null;
        }
    }

    boolean personelSil(String no) {
        try {
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("DELETE FROM `PERSONELLER` WHERE `NO`='"+no+"'");
            close();
            return true;
        } catch (SQLException ex) {
            close();
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    void secilenTaksitiSifirla(String no, String tarih, String tutar,String grup) {
        try {
            qosul(getCurrentYear());
            for(int i=1;i<9;i++)
            {
                stmt=conn.createStatement();
                stmt.executeUpdate("UPDATE `TAKSITLER` SET `"+i+"TAKSITTUTAR`='0' WHERE (`NO`='"+no+"' AND `"+i+"TAKSITTUTAR`='"+tutar+"' AND `"+i+"TAKSITTARIH`='"+tarih+"'AND `GRUP`='"+grup+"')");
            }
            close();
        } catch (SQLException ex) {
            close();
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        close();
    }

    void secilenSinaviSil(String sinav) {
        try {
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            stmt.executeUpdate("DELETE FROM `IMTAHANLAR` WHERE `AD`='"+sinav+"'");
            stmt=conn.createStatement();
            stmt.executeUpdate("DROP TABLE `"+sinav+"`");
            close();
        } catch (SQLException ex) {
            close();
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
        close();
        
    }

    boolean taksitVarmi(String no, String grup) {
        try {
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            rs=stmt.executeQuery("SELECT COUNT(*) FROM `TAKSITLER` WHERE(`NO`='"+no+"' AND `GRUP`='"+grup+"')");
            rs.next();
            System.out.println(rs.getInt(1));
            if(rs.getInt(1)<=0)
            {
                close();
                return false;
            }
            close();
            return true;
        } catch (SQLException ex) {
            close();
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
    }

    void taksitiSil(String no, String grup) {
        try {
            qosul(getCurrentYear());
            stmt=conn.createStatement();
            System.out.println(""+no+" "+grup);
            stmt.executeUpdate("DELETE FROM `TAKSITLER` WHERE `NO`='"+no+"' AND `GRUP`='"+grup+"' ");
            close();
        } catch (SQLException ex) {
            close();
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    String[][] bugunOdenecekTaksitler(String[][] matrix,String[] basliq,boolean gecikmismi,boolean bugunmu) {
    
        String[][] filtrelenmisMatrix= new String[matrix.length][basliq.length];
        boolean yukle=true;
        int iF=0;
        for(int iMatrix = 0; iMatrix < matrix.length ;iMatrix++ )
        {   
            yukle=false;

            if(bugunmu && matrix[iMatrix][5]!=null && matrix[iMatrix][5].contains("0"))
            {
                yukle=true;
            }
            else if(gecikmismi && matrix[iMatrix][5]!=null && matrix[iMatrix][5].contains("-"))
            {
                yukle=true;
            }
            
            if(yukle)
            {  
                for(int j=0;j<basliq.length;j++)
                {
                    filtrelenmisMatrix[iF][j]=matrix[iMatrix][j];
                }
                iF++;
            }
        }
        return filtrelenmisMatrix;
    }
}
    