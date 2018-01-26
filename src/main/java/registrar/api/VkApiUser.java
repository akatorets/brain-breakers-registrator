package registrar.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.newsfeed.NewsfeedGetFilter;
import registrar.domain.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs operations with vk API.
 */
public class VkApiUser {
    private VkApiClient vkApiClient = new VkApiClient(HttpTransportClient.getInstance());
    private JsonParser jsonParser = new JsonParser();
    private Gson gson = new Gson();

    /**
     * Return specified number of recent posts from specified source.
     * @param userActor the user on whose behalf the request is made
     * @param sourceId source
     * @param count number of posts
     * @return specified number of recent posts
     * @throws ClientException on vk API client error
     */
    public List<Post> getPosts(UserActor userActor, String sourceId, Integer count) throws ClientException {
        String posts = vkApiClient.newsfeed()
                .get(userActor)
                .sourceIds(sourceId)
                .filters(NewsfeedGetFilter.POST)
                .count(count)
                .executeAsString();
        return convertFromJson(posts);
    }

    /**
     * Return last post from specified source.
     * @param userActor the user on whose behalf the request is made
     * @param sourceId source
     * @return the top post
     * @throws ClientException on vk API client error
     */
    public Post getTopPost(UserActor userActor, String sourceId) throws ClientException {
        List<Post> posts = getPosts(userActor, sourceId, 1);
        return posts.iterator().next();
    }

    /**
     * Send a comment under the post.
     * @param userActor the user on whose behalf the comment is posted
     * @param post the post under which you need to leave a comment
     * @param message comment message
     * @throws ClientException on vk API client error
     * @throws ApiException on vk API error
     */
    public void createComment(UserActor userActor, Post post, String message) throws ClientException, ApiException {
        vkApiClient.wall()
                .createComment(userActor, post.getPostId())
                .ownerId(post.getSourceId())
                .message(message)
                .execute();
    }

    /**
     * Send a message.
     * @param userActor the user on whose behalf the message is sent
     * @param peerId the destination user id
     * @param message message
     * @throws ClientException on vk API client error
     * @throws ApiException on vk API error
     */
    public void sendMessage(UserActor userActor, Integer peerId, String message) throws ClientException, ApiException {
        vkApiClient.messages()
                .send(userActor)
                .peerId(peerId)
                .message(message)
                .execute();
    }

    /**
     * Make a repost of given post.
     * @param userActor the user which makes a repost
     * @param post required post
     * @throws ClientException on vk API client error
     * @throws ApiException on vk API error
     */
    public void repost(UserActor userActor, Post post) throws ClientException, ApiException {
        String object = "wall" + post.getSourceId() + "_" + post.getPostId();
        vkApiClient.wall()
                .repost(userActor, object)
                .execute();
    }

    private List<Post> convertFromJson(String responseJson) {
        JsonObject response = jsonParser.parse(responseJson).getAsJsonObject().getAsJsonObject("response");
        JsonArray items = response.getAsJsonArray("items");

        List<Post> posts = new ArrayList<>();
        for (JsonElement element: items) {
            posts.add(gson.fromJson(element, Post.class));
        }
        return posts;
    }

}
