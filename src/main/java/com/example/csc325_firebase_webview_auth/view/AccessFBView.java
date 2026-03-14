package com.example.csc325_firebase_webview_auth.view;//package modelview;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.util.Callback;




public class AccessFBView {
    Stage stage;
    @FXML
    private TableColumn<Person, String> name_col = new TableColumn<>("Name");
    @FXML
    private TableColumn<Person, String> age_col = new TableColumn<>("Age");
    @FXML
    private TableColumn<Person, String> major_col = new TableColumn<>("Major");
    @FXML
    private TableView tableView;
    @FXML
    Button upload;
    private TextField email_txt;
    @FXML
    private PasswordField pass_txt;
    @FXML
    private ImageView img;
    @FXML
    private TextField phone_txt;
    @FXML
    private TextField disName_txt;
     @FXML
    private TextField nameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField ageField;
    @FXML
    private Button writeButton;
    @FXML
    private Button readButton;
    @FXML
    private TextArea outputField;
     private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;
    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }


    void initialize() {

        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        nameField.textProperty().bindBidirectional(accessDataViewModel.userNameProperty());
        majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
    }

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

        @FXML
    private void readRecord(ActionEvent event) {
        readFirebase();
    }

            @FXML
    private void regRecord(ActionEvent event) {
        registerUser();
    }

     @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("/files/WebContainer.fxml");
    }

    public void addData() {

        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameField.getText());
        data.put("Major", majorField.getText());
        data.put("Age", Integer.parseInt(ageField.getText()));
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }

    @FXML
    private void profilePic(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Profile Picutre");
        // Show the dialog
        File file = fileChooser.showOpenDialog(stage); // You can pass a stage reference here
        if (file != null) {
            // Do something interesting with the file, such as reading its contents
            uploadToFB(String.valueOf(file));
           // Image pic = new Image(String.valueOf(file.getAbsolutePath()));
          //  img.setImage(pic);
            System.out.println("Selected file: " + file.getAbsolutePath());
        }

    }

    private void uploadToFB(String file) throws IOException {
        InputStream fileInputStream = new FileInputStream(file);
        byte[] fileBytes = Files.readAllBytes(Paths.get(file));
        String blobName = (file);
        App.bucket.create(blobName, fileInputStream, "image/jpeg");
        System.out.println("File " + file + " uploaded to Storage");
    }



        public boolean readFirebase()
         {
             key = false;

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future =  App.fstore.collection("References").get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        try
        {
            documents = future.get().getDocuments();
            if(documents.size()>0)
            {
                System.out.println("Outing....");
                for (QueryDocumentSnapshot document : documents)
                {
                    outputField.setText(outputField.getText()+ document.getData().get("Name")+ " , Major: "+
                            document.getData().get("Major")+ " , Age: "+
                            document.getData().get("Age")+ " \n ");
                    System.out.println(document.getId() + " => " + document.getData().get("Name"));

                    name_col.setCellValueFactory(new PropertyValueFactory<>("Name"));
                    major_col.setCellValueFactory(new PropertyValueFactory<>("Major"));
                    age_col.setCellValueFactory(new PropertyValueFactory<>("Age"));
                    tableView.setItems(getListOfUsers());

                    person  = new Person(String.valueOf(document.getData().get("Name")),
                            document.getData().get("Major").toString(),
                            Integer.parseInt(document.getData().get("Age").toString()));
                    listOfUsers.add(person);
                }
            }
            else
            {
               System.out.println("No data");
            }
            key=true;

        }
        catch (InterruptedException | ExecutionException ex)
        {
             ex.printStackTrace();
        }
        return key;
    }

        public void sendVerificationEmail() {
        try {
            UserRecord user = App.fauth.getUser("name");
            //String url = user.getPassword();

        } catch (Exception e) {
        }
    }

    public void BackToLogIn() throws IOException {
        App.setRoot("/files/WebContainer.fxml");
    }

    public String getEmail(){
        return email_txt.getText();
    }

    public String getPass(){
        return pass_txt.getText();
    }

    public String getPhone(){
        return phone_txt.getText();
    }

    public String getDisName(){
        return disName_txt.getText();
    }



    public boolean registerUser() {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(getEmail())
                .setEmailVerified(false)
                .setPassword(getPass())
                .setPhoneNumber(getPhone())
                .setDisplayName(getDisName())
                .setDisabled(false);

        UserRecord userRecord;
        try {
            userRecord = App.fauth.createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());
            return true;

        } catch (FirebaseAuthException ex) {
           // Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }
}
