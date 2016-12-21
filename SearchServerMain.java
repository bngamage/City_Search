//Copyright 2016, University of Freiburg,
//Chair of Algorithms and Data Structures.
//Author: Hannah Bast <bast@cs.uni-freiburg.de>, Bhashitha Gamage <gamage25@gmail.com>

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;

/**
 * Demo search server.
 */
public class SearchServerMain {

  public static void main(String[] args) throws IOException {

    
    // Parse the command line arguments.
    if (args.length < 2) {
      System.out.println("Usage: java -jar SearchServerMain.jar <file> <port>");
      System.exit(1);
    }
    String inputFile = args[0];
    int port = Integer.parseInt(args[1]);

    // create socket
    ServerSocket server = new ServerSocket(port);
    BufferedWriter out = null;

    // Building QgramIndex
    String fileName = inputFile;
    System.out.print("Reading strings and building Qgram index...\n");
    QGramIndex qgi = new QGramIndex(3);
    qgi.buildFromFile(fileName);
    System.out.print(" done.\n");

    while (true) {
      System.out.print("Waiting for query on port " + port + " ...");
      // wait for request, wait until sopmeone calls
      Socket client = server.accept();

      System.out.println("client connected from " + client.getInetAddress());

      // reading the request
      BufferedReader input =
        new BufferedReader(new InputStreamReader(client.getInputStream()));
      String request = input.readLine();
      System.out.println("Request string is " + request);

      // Process request.
      byte[] contentBytes = new byte[0];
      String contentType = "text/plain";
      String contentStatus = "HTTP/1.1 200 OK";

      if (!request.startsWith("GET /")) {
        contentBytes = "I only answer get requests, sorry!".getBytes("UTF-8");
      } else {
        // extract the exact filename that requesting.
        request = request.substring(5, request.indexOf(" HTTP/1.1"));

        // modify the request regex to exclude ?, = symbols.
        // this will prevent accessing unnecessary files and javaScript
        // injection in primary way.
        if (!request.matches("[A-Za-z0-9?=.,%-()]+$")) {
          contentStatus = "HTTP/1.1 403 You are a very bad person";
        } else if (!request.contains("?q")) {
          System.out.println("This is a file request");

          File file = new File(request);
          if (file.canRead()) {
            // file exists, return the content
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            contentBytes = bytes;
            // change the content type based on the request.
            if (request.endsWith(".html")) {
              contentType = "text/html";
            } else if (request.endsWith(".css")) {
              contentType = "text/css";
            } else if (request.endsWith(".javascript")) {
              contentType = "application/javascript";
            } else if (request.endsWith(".png")) {
              contentType = "image/png";
            }
          } else {
            contentBytes = "File does not exists".getBytes("UTF-8");
            contentStatus = "HTTP/1.1 404 File not found";
          }
        } else {
          contentType = "application/json";
          System.out.println("This is a query request");

          // extract the query(?q=) out from the request.
          String query = request.substring(3);
          query = SearchServerMain.urlDecode(query);
          String normalizedQuery = qgi.normalizeString(query);
          int delta = (normalizedQuery.length() / 4);

          contentBytes = qgi.findMatches(query, delta).getBytes("UTF-8");

        }
      }

      // sening the client something - not provided in the wiki sample code
      DataOutputStream output = new DataOutputStream(client.getOutputStream());
      // String response = "Tell me more about "+ request;
      StringBuilder response = new StringBuilder();
      response.append(contentStatus + "\r\n");
      response.append("Content-Length: " + contentBytes.length + "\r\n");
      response.append("Content-Type: " + contentType + "\r\n");
      response.append("\r\n");
      output.write(response.toString().getBytes("UTF-8"));
      output.write(contentBytes);
      
      BufferedWriter responseWriter =
        new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
      responseWriter.write("\n Go away!\n");
      responseWriter.flush();
      responseWriter.close();

      }
  }

  public static String urlDecode(String encoded) {
    String decoded = "";
    try {
      decoded = URLDecoder.decode(encoded, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return decoded;
  }
}
