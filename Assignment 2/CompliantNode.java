import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import static java.util.stream.Collectors.toSet;
/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private final Double p_graph;
    private final Double p_malicious;
    private final Double p_txDistribution;
    private final Integer numRounds;
    private Set<Transaction> pendingTransactions;
    private boolean[] followees;
    private Set<Integer> blackList;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
        this.blackList = new HashSet<>(followees.length);
        return;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.pendingTransactions = pendingTransactions;
        return;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        return new HashSet<>(this.pendingTransactions);
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        final Set<Integer> senders = candidates.stream().map(candidate -> candidate.sender).collect(toSet());
        for(int i = 0; i < this.followees.length; i++){
          if (this.followees[i] && !senders.contains(i)) this.blackList.add(i);
        }
        this.pendingTransactions = candidates.stream()
                .filter(candidate -> !this.blackList.contains(candidate.sender))
                .map(candidate -> candidate.tx)
                .collect(toSet());
        return;
    }
}
