package si.medius.makeit.recommendation;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/recommend")
public class RecommendationResource
{
    @Inject
    RecommendationService recommendationService;

    @GET
    @Path("/{userId}")
    public List<String> getRecommendations(@PathParam("userId") String userId) throws IOException
    {
        return recommendationService.getRecommendedItemsForUser(userId);
    }
}
