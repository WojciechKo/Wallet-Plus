package pl.net.korzeniowski.walletplus.sync.google;

import com.google.common.collect.Lists;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

public interface GoogleDriveUploadService {

    @Multipart
    @POST("/files?uploadType=multipart")
    void insert(@Part("metadata") FileMetadata metadata, @Part("data") TypedFile file, @Header("Authorization") String authorization, Callback<FileMetadata> cb);

    @PUT("/files/{fileId}?uploadType=media")
    void update(@Path("fileId") String fileId, @Body TypedFile file, @Header("Authorization") String authorization, Callback<FileMetadata> cb);

    class FileMetadata {
        String id;
        String title;
        List<Parent> parents = Lists.newArrayList();

        public String getId() {
            return id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setParentId(String parentId) {
            parents.add(new Parent().setId(parentId));

        }

        public class Parent {
            String id;

            public Parent setId(String id) {
                this.id = id;
                return this;
            }

            public String getId() {
                return id;
            }
        }
    }
}
