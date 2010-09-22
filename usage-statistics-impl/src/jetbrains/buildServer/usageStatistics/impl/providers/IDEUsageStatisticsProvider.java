package jetbrains.buildServer.usageStatistics.impl.providers;

import java.util.*;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.impl.XmlRpcBasedRemoteServer;
import jetbrains.buildServer.serverSide.impl.XmlRpcDispatcher;
import jetbrains.buildServer.serverSide.impl.XmlRpcListener;
import jetbrains.buildServer.serverSide.impl.XmlRpcSession;
import jetbrains.buildServer.usageStatistics.UsageStatisticsProvider;
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsFormatter;
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager;
import jetbrains.buildServer.usageStatistics.presentation.formatters.TypeBasedFormatter;
import jetbrains.buildServer.usageStatistics.util.BasePersistentStateComponent;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.filters.Filter;
import jetbrains.buildServer.util.filters.FilterUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Manuylov
 *         Date: 21.09.2010
 */
public class IDEUsageStatisticsProvider extends BaseDynamicUsageStatisticsProvider implements UsageStatisticsProvider, XmlRpcListener {
  @NonNls @NotNull private static final String IDE_USAGE_GROUP = "IDE Usage";

  @NotNull private final Map<String, Set<IDEUsage>> myIDEUsages = new HashMap<String, Set<IDEUsage>>();

  public IDEUsageStatisticsProvider(@NotNull final SBuildServer server,
                                    @NotNull final ServerPaths serverPaths,
                                    @NotNull final XmlRpcDispatcher xmlRpcDispatcher,
                                    @NotNull final UsageStatisticsPresentationManager presentationManager) {
    super(server, presentationManager);
    registerPersistor(server, serverPaths);
    xmlRpcDispatcher.addListener(this);
  }

  public void remoteMethodCalled(@NotNull final Class targetClass,
                                 @NotNull final String methodName,
                                 @NotNull final Vector params,
                                 @Nullable final XmlRpcSession session) {
    if (targetClass == XmlRpcBasedRemoteServer.class && session != null) {
      final Long userId = session.getUserId();
      if (userId != null) {
        addUsage(prepareUserAgent(session.getUserAgent()), userId);
      }
    }
  }

  @Override
  protected void accept(@NotNull final UsageStatisticsPublisher publisher, @NotNull final String periodDescription, final long startDate) {
    final Map<String, Set<IDEUsage>> usages = filterUsages(startDate);
    final UsageStatisticsFormatter formatter = createFormatter(usages);
    for (final Map.Entry<String, Set<IDEUsage>> entry : usages.entrySet()) {
      final String ideName = entry.getKey();
      final String statisticId = "jetbrains.buildServer.usageStatistics.ideUsage." + ideName.replace(' ', '.') + ".ForTheLast" + periodDescription;
      myPresentationManager.applyPresentation(statisticId, ideName + " user count for the last " + periodDescription.toLowerCase(), IDE_USAGE_GROUP, formatter);
      publisher.publishStatistic(statisticId, entry.getValue().size());
    }
  }

  @NotNull
  private String prepareUserAgent(@NotNull final String userAgent) {
    int endPos = userAgent.indexOf('/');
    if (endPos == -1) {
      endPos = userAgent.indexOf('(');
    }
    else {
      endPos = userAgent.indexOf('.', endPos + 1);
    }

    String preparedUserAgent = userAgent.replace('/', ' ');
    if (endPos != -1) {
      preparedUserAgent = preparedUserAgent.substring(0, endPos);
    }

    return preparedUserAgent.trim();
  }

  private UsageStatisticsFormatter createFormatter(@NotNull final Map<String, Set<IDEUsage>> usages) {
    final int totalUsages = getTotalUsagesCount(usages);
    return new TypeBasedFormatter<Integer>(Integer.class) {
      @Override
      protected String doFormat(@NotNull final Integer count) {
        return count + " (" + (count * 100 / totalUsages) + "%)";
      }
    };
  }

  private int getTotalUsagesCount(@NotNull final Map<String, Set<IDEUsage>> usages) {
    int totalUsages = 0;
    for (final Set<IDEUsage> ideUsages : usages.values()) {
      totalUsages += ideUsages.size();
    }
    return totalUsages;
  }

  @NotNull
  private synchronized Map<String, Set<IDEUsage>> filterUsages(final long startDate) {
    final Map<String, Set<IDEUsage>> result = new HashMap<String, Set<IDEUsage>>();
    for (final Map.Entry<String, Set<IDEUsage>> entry : myIDEUsages.entrySet()) {
      final HashSet<IDEUsage> filteredUsages = FilterUtil.filterAndCopy(entry.getValue(), new HashSet<IDEUsage>(), new Filter<IDEUsage>() {
        public boolean accept(@NotNull final IDEUsage usage) {
          return usage.getTimestamp() > startDate;
        }
      });
      if (!filteredUsages.isEmpty()) {
        result.put(entry.getKey(), filteredUsages);
      }
    }
    return result;
  }

  private synchronized void addUsage(@NotNull final String ideName, final long userId) {
    if (!myIDEUsages.containsKey(ideName)) {
      myIDEUsages.put(ideName, new HashSet<IDEUsage>());
    }
    final Set<IDEUsage> ideUsages = myIDEUsages.get(ideName);
    final IDEUsage usage = new IDEUsage(String.valueOf(userId), Dates.now().getTime());
    ideUsages.remove(usage);
    ideUsages.add(usage);
  }

  @NonNls @NotNull private static final String IDE = "ide";
  @NonNls @NotNull private static final String NAME = "name";
  @NonNls @NotNull private static final String USAGE = "usage";
  @NonNls @NotNull private static final String USER_ID = "userId";
  @NonNls @NotNull private static final String TIMESTAMP = "timestamp";

  private synchronized void writeExternal(@NotNull final Element element) {
    final long thresholdDate = getThresholdDate();
    for (final Map.Entry<String, Set<IDEUsage>> entry : myIDEUsages.entrySet()) {
      final Element ideElement = new Element(IDE);
      ideElement.setAttribute(NAME, entry.getKey());
      element.addContent(ideElement);
      for (final IDEUsage usage : entry.getValue()) {
        if (usage.getTimestamp() <= thresholdDate) continue;
        final Element usageElement = new Element(USAGE);
        usageElement.setAttribute(USER_ID, usage.getUserId());
        usageElement.setAttribute(TIMESTAMP, String.valueOf(usage.getTimestamp()));
        ideElement.addContent(usageElement);
      }
    }
  }

  private synchronized void readExternal(@NotNull final Element element) {
    myIDEUsages.clear();
    final long thresholdDate = getThresholdDate();
    for (final Object ide : element.getChildren(IDE)) {
      if (!(ide instanceof Element)) continue;
      final Element ideElement = (Element)ide;
      final String name = ideElement.getAttributeValue(NAME);
      if (name == null) continue;
      myIDEUsages.put(name, new HashSet<IDEUsage>());
      for (final Object usage : ideElement.getChildren(USAGE)) {
        if (!(usage instanceof Element)) continue;
        final Element usageElement = (Element)usage;
        final String userId = usageElement.getAttributeValue(USER_ID);
        if (userId == null) continue;
        final String timestampStr = usageElement.getAttributeValue(TIMESTAMP);
        if (timestampStr == null) continue;
        try {
          final long timestamp = Long.parseLong(timestampStr);
          if (timestamp > thresholdDate) {
            myIDEUsages.get(name).add(new IDEUsage(userId, timestamp));
          }
        }
        catch (final NumberFormatException ignore) {}
      }
    }
  }

  private void registerPersistor(@NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
    new BasePersistentStateComponent(server, serverPaths) {
      @NotNull
      @Override
      protected String getId() {
        return "ideUsage";
      }

      @Override
      protected void writeExternal(@NotNull final Element element) {
        IDEUsageStatisticsProvider.this.writeExternal(element);
      }

      @Override
      protected void readExternal(@NotNull final Element element) {
        IDEUsageStatisticsProvider.this.readExternal(element);
      }
    };
  }

  private static class IDEUsage {
    @NotNull private final String myUserId;
    private final long myTimestamp;

    public IDEUsage(@NotNull final String userId, final long timestamp) {
      myUserId = userId;
      myTimestamp = timestamp;
    }

    @NotNull
    public String getUserId() {
      return myUserId;
    }

    public long getTimestamp() {
      return myTimestamp;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof IDEUsage)) return false;
      return myUserId.equals(((IDEUsage)o).myUserId);
    }

    @Override
    public int hashCode() {
      return myUserId.hashCode();
    }
  }
}