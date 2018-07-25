// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {
    private class blockNode{
      public Block b;
      public blockNode parent;
      public ArrayList<blockNode> children;
      public int h; // height
      private UTXOPool utxopool;
      public blockNode(Block b, blockNode parent, UTXOPool utxopool) {
            this.b = b;
            this.parent = parent;
            children = new ArrayList<>();
            this.utxopool = utxopool;
            if (parent != null) {
                h = parent.h + 1;
                parent.children.add(this);
            } else {
                h = 1;
            }
        }
      public UTXOPool getUTXOPoolCopy() {
            return new UTXOPool(utxopool);
      }
    }
    private HashMap<ByteArrayWrapper, blockNode> blockChain;
    private blockNode maxHeightNode;
    private TransactionPool txPool;
    public static final int CUT_OFF_AGE = 10;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        blockChain = new HashMap<>();
        UTXOPool utxoPool = new UTXOPool();
        addCoinbaseToUTXOPool(genesisBlock, utxoPool);
        blockNode genesisNode = new blockNode(genesisBlock, null, utxoPool);
        blockChain.put(wrap(genesisBlock.getHash()), genesisNode);
        txPool = new TransactionPool();
        maxHeightNode = genesisNode;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return maxHeightNode.b;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return maxHeightNode.getUTXOPoolCopy();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     *
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        byte[] prevBlockHash = block.getPrevBlockHash();
       if (prevBlockHash == null)
           return false;
       blockNode parentBlockNode = blockChain.get(wrap(prevBlockHash));
       if (parentBlockNode == null) {
           return false;
       }
       TxHandler handler = new TxHandler(parentBlockNode.getUTXOPoolCopy());
       Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);
       Transaction[] validTxs = handler.handleTxs(txs);
       if (validTxs.length != txs.length) {
           return false;
       }
       int proposedHeight = parentBlockNode.h + 1;
       if (proposedHeight <= maxHeightNode.h - CUT_OFF_AGE) {
           return false;
       }
       UTXOPool utxoPool = handler.getUTXOPool();
       addCoinbaseToUTXOPool(block, utxoPool);
       blockNode node = new blockNode(block, parentBlockNode, utxoPool);
       blockChain.put(wrap(block.getHash()), node);
       if (proposedHeight > maxHeightNode.h) {
           maxHeightNode = node;
       }
       return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
         txPool.addTransaction(tx);

    }

    // Helper functions
    private void addCoinbaseToUTXOPool(Block block, UTXOPool utxoPool) {
       Transaction coinbase = block.getCoinbase();
       for (int i = 0; i < coinbase.numOutputs(); i++) {
           Transaction.Output out = coinbase.getOutput(i);
           UTXO utxo = new UTXO(coinbase.getHash(), i);
           utxoPool.addUTXO(utxo, out);
       }
   }

   private static ByteArrayWrapper wrap(byte[] arr) {
       return new ByteArrayWrapper(arr);
   }
}
