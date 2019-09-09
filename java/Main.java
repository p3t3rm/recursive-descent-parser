import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            System.err.println("no arguments expected");
            return;
        }
        System.out.println("enter query, end with ctrl-d");

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String query = br.lines().collect(Collectors.joining("\n"));

        AST ast = parse(query);
        System.out.println("Ok.");
    }

    static interface AST {
    }
   
    static class Or implements AST {
        AST left;
        AST right;
    }

    static class And implements AST {
        AST left;
        AST right;
    }

    static class NameValue implements AST {
        String name;
        String value;
    }

    static AST parse(String query) throws Exception {
        StreamTokenizer tok = new StreamTokenizer(new StringReader(query));
        tok.quoteChar('\"');
        AST ast = parseTree(tok);
        tok.nextToken();
        if (tok.ttype != StreamTokenizer.TT_EOF) {
            throw invalidQuery("expected end-of-input");
        }
        return ast;
    }

    static AST parseTree(StreamTokenizer tok) throws Exception {
        AST node = parseNode(tok);
        tok.nextToken();
        switch (tok.ttype) {
        case StreamTokenizer.TT_WORD:
            switch (tok.sval) {
            case "or":
                Or or = new Or();
                or.left = node;
                or.right = parseTree(tok);
                return or;
            case "and":
                And and = new And();
                and.left = node;
                and.right = parseTree(tok);
                return and;
            default:
                throw invalidQuery("expected 'or' or 'and'");
            }
        default:
            tok.pushBack();
            return node;
        }
    }        
   
    static AST parseNode(StreamTokenizer tok) throws Exception {
        tok.nextToken();
        switch (tok.ttype) {
        case StreamTokenizer.TT_WORD:
            NameValue nv = new NameValue();
            nv.name = tok.sval;
            tok.nextToken();
            if (tok.ttype != '=') {
                throw invalidQuery("expected '='");
            }
            tok.nextToken();
            if (tok.ttype != '"') {
                throw invalidQuery("expected '\"'");
            }
            nv.value = tok.sval;
            return nv;
        case '(':
            AST tree = parseTree(tok);
            if (tok.nextToken() != ')') {
                throw invalidQuery("expected ')'");
            }
            return tree;
        default:
            throw invalidQuery("expected name=\"value\" or '('");
        }
    }

    static Exception invalidQuery(String msg) {
        return new Exception(msg);
    }
   
}
