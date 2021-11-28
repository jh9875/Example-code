import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsRequest;
import software.amazon.awssdk.services.cloudwatch.model.ListMetricsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;

public class AwsCloudWatchExample {

	public static void main(String args[]) {
		String accessKeyId = "";
		String secretAccessKey = "";
		String region = "";
		String metricName = "";

		CloudWatchClient client = createClient(accessKeyId, secretAccessKey, region);

		Instant startTimeInstant = Instant.now().minus(Duration.ofDays(3));
		Instant endTimeInstant = Instant.now();

		List<Metric> metrics = listMetrics(client, metricName);
		List<MetricDataQuery> metricDataQueries = getMetricDataQueries(metrics);

		List<MetricDataResult> metricDataResults = getMetricData(client, metricDataQueries, startTimeInstant,
				endTimeInstant);
		
		for(MetricDataResult metricDataResult : metricDataResults) {
			
		}
	}

	public static CloudWatchClient createClient(String accessKeyId, String secretAccessKey, String region) {
		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

		StaticCredentialsProvider staticCredentialProvider = StaticCredentialsProvider.create(awsCredentials);

		CloudWatchClient client = CloudWatchClient.builder().credentialsProvider(staticCredentialProvider)
				.region(Region.of(region)).build();

		return client;
	}

	public static List<Metric> listMetrics(CloudWatchClient client, String metricName) {
		List<Metric> metrics = new ArrayList<>();

		try {
			ListMetricsRequest request = ListMetricsRequest.builder().metricName(metricName).build();
			ListMetricsResponse response = client.listMetrics(request);
			metrics.addAll(response.metrics());
			String nextToken = response.nextToken();

			while (nextToken != null) {
				ListMetricsRequest nextRequest = ListMetricsRequest.builder().nextToken(nextToken).build();
				ListMetricsResponse nextResponse = client.listMetrics(nextRequest);
				metrics.addAll(nextResponse.metrics());
				nextToken = nextResponse.nextToken();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return metrics;
	}

	public static List<MetricDataQuery> getMetricDataQueries(List<Metric> metrics) {
		List<MetricDataQuery> metricDataQueries = new ArrayList<>();
		int id = 0;

		for (Metric metric : metrics) {
			MetricStat metricStat = MetricStat.builder().metric(metric).period(60).stat("Average").unit("Percent")
					.build();
			MetricDataQuery metricDataQuery = MetricDataQuery.builder().id("m" + id).metricStat(metricStat).build();
			metricDataQueries.add(metricDataQuery);
		}

		return metricDataQueries;
	}

	public static List<MetricDataResult> getMetricData(CloudWatchClient client, List<MetricDataQuery> metricDataQueries,
			Instant startTimeInstant, Instant endTimeInstant) {
		List<MetricDataResult> metricDataResults = new ArrayList<>();

		try {
			GetMetricDataRequest request = GetMetricDataRequest.builder().metricDataQueries(metricDataQueries)
					.startTime(startTimeInstant).endTime(endTimeInstant).build();
			GetMetricDataResponse response = client.getMetricData(request);
			metricDataResults.addAll(response.metricDataResults());
			String nextToken = response.nextToken();

			while (nextToken != null) {
				GetMetricDataRequest nextRequest = GetMetricDataRequest.builder().startTime(startTimeInstant)
						.endTime(endTimeInstant).nextToken(nextToken).build();
				GetMetricDataResponse nextResponse = client.getMetricData(nextRequest);
				metricDataResults.addAll(nextResponse.metricDataResults());
				nextToken = nextResponse.nextToken();
			}

		} catch (CloudWatchException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return metricDataResults;
	}
}
