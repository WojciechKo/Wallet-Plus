package info.korzeniowski.walletplus.sync.google;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Path;

public interface GoogleDriveReadService {

    @GET("/files/{fileId}")
    void getFile(@Path("fileId") String fileId, Callback<DriveFile> cb);

    @GET("/files/{folderId}/children?fields=items%2Fid")
    void getChildren(@Path("folderId") String folderId, Callback<FileChildren> cb);

    @DELETE("/files/{fileId}/")
    void delete(@Path("fileId") String fileId, Callback<Response> cb);

    class DriveFile {
        String id;
        String title;
        String downloadUrl;
        Long fileSize;
        Date createdDate;
        Date modifiedDate;
        List<Owner> owners;

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public Date getCreatedDate() {
            return createdDate;
        }

        public Date getModifiedDate() {
            return modifiedDate;
        }

        public String getOwner() {
            return owners.isEmpty()
                    ? ""
                    : owners.get(0).getEmailAddress();
        }
    }

    class Owner {
        String emailAddress;

        public String getEmailAddress() {
            return emailAddress;
        }
    }

    class FileChildren {
        @SerializedName("items")
        List<FileId> children;

        public List<FileId> getChildren() {
            return children;
        }

        public class FileId {
            String id;

            public String getId() {
                return id;
            }
        }
    }
}
