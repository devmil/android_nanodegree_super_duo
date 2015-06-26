package it.jaschke.alexandria.api.books;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

public interface GoogleBooksAPI {

    String GOOGLE_APIS_BASE_URL = "https://www.googleapis.com";

    @GET("/books/v1/volumes")
    SearchResponse searchItems(@Query("q")String searchQuery);

    class SearchResponse {
        public String kind;
        public int totalItems;
        public List<Item> items;
    }

    class Item {
        public String kind;
        public String id;
        public String etag;
        public String selfLink;
        public VolumeInfo volumeInfo;
        public SaleInfo saleInfo;
        public AccessInfo accessInfo;
        public SearchInfo searchInfo;
    }

    class VolumeInfo {
        public String title;
        public String subtitle;
        public List<String> authors;
        public String publisher;
        public String publishedDate;
        public String description;
        public List<IndustryIdentifier> industryIdentifiers;
        public ReadingModes readingModes;
        public int pageCount;
        public String printType;
        public List<String> categories;
        public float averageRating;
        public int ratingsCount;
        public String maturityRating;
        public boolean allowAnonLogging;
        public String contentVersion;
        public ImageLinks imageLinks;
        public String language;
        public String previewLink;
        public String infoLink;
        public String canonicalVolumeLink;
    }

    class IndustryIdentifier {
        public String type;
        public String identifier;
    }

    class ReadingModes {
        public boolean text;
        public boolean image;
    }

    class SaleInfo {
        public String country;
        public String saleability;
        public boolean isEbook;
    }

    class AccessInfo {
        public String country;
        public String viewability;
        public boolean embeddable;
        public boolean publicDomain;
        public String textToSpeechPermission;
        public EPubInfo epub;
        public PdfInfo pdf;
        public String webReaderLink;
        public String accessViewStatus;
        public boolean quoteSharingAllowed;
    }

    class EPubInfo {
        public boolean isAvailable;
        public String acsTokenLink;
    }

    class PdfInfo {
        public boolean isAvailable;
    }

    class SearchInfo {
        public String textSnippet;
    }

    class ImageLinks {
        public String smallThumbnail;
        public String thumbnail;
    }
}
