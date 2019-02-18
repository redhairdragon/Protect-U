package com.pgzxc.nfcbeam;

import org.json.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;


public class scannedIdEntry {
    public int primaryKey=99854;
    public String studentId="104758168";

    private String generateHash(){
        String hex=new String();
        long timestamp=TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(timestamp);
        bb.array();
        try{
            MessageDigest md = MessageDigest.getInstance( "SHA-256" );
            md.update( studentId.getBytes());
            md.update(bb.array());
            byte[] digest = md.digest();
            hex = String.format( "%064x", new BigInteger( 1, digest) );
        }catch (NoSuchAlgorithmException e){}
        return hex;
    }

    public String generateJsonStr(){
        String str=new String();
        try {
            JSONObject IdEntry=new JSONObject();
            IdEntry.put("IDHash",generateHash());
            IdEntry.put("primaryKey",primaryKey);
            str=IdEntry.toString();
        }
        catch(JSONException ex){}
        return str;
    }
}
