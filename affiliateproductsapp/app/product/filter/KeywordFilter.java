package product.filter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Product;

import org.apache.commons.lang.StringUtils;

public class KeywordFilter extends BaseProductFilter {

	public KeywordFilter(CriteriaBuilder cb, CriteriaQuery cq,
			Root<Product> product) {
		super(cb, cq, product);
	}


	@Override
	public List<Predicate> filter(List<Long> sellerIds, List<Long> brandIds,
			List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice,
			Integer sale, String keyword) {

		List<Predicate> predicates = new ArrayList<Predicate>();
		if (keyword != null && keyword.length() > 0) {
			List<String> output = new ArrayList<String>();
			
			output.add("%" + keyword + "%");
						
			Set<String> inputStringSet = new HashSet<String>(Arrays.asList(keyword.split(" ")));
			List<Set<String>> inputComb = permutations(inputStringSet);
			for(Set<String> inputSet:inputComb){
				String input = StringUtils.join(inputSet.toArray(), " ");
				output.add("%" + input + "%");
			}
			
			for(Set<String> inputSet:inputComb){
				String input = StringUtils.join(inputSet.toArray(), " ");
				output.add("%" + input.replace(" ", "%") + "%");
			}
			
			System.out.println("-------------------------");
			System.out.println(output);
			System.out.println("-------------------------");
			
//			Predicate predicate = cb.like(product.<String>get ("name"), "%" + keyword + "%");
//			predicates.add(predicate);
			
//			Predicate predicate1 = cb.like(product.<String>get ("name"), "%" + keyword + "%");
//			Predicate predicate2 = cb.like(product.<String>get ("description"), "%" + keyword + "%");
//			Predicate predicate = cb.or(predicate1, predicate2);
//			predicates.add(predicate);
			
			
			List<Predicate> tmpPredicates = new ArrayList<Predicate>();
			for(String input:output){
				Predicate predicate = cb.like(product.<String>get ("name"), input);
				tmpPredicates.add(predicate);				
			}
			System.out.println(tmpPredicates);
			
			//Predicate[] predicateArray = (Predicate[]) tmpPredicates.toArray();
			//System.out.println(predicateArray);
			
			Predicate predicate = null;
			for(Predicate pred:tmpPredicates){
				if(predicate == null){
					predicate = cb.or(pred);
				}else{
					predicate = cb.or(predicate, pred);
				}				
			}
			

			predicates.add(predicate);
			
			
			
		}
		return predicates;
	}
	
	public List<Set<String>> permutations(final Set<String> input) {
	    if (input.size() == 1) {
	        return Arrays.asList(input);
	    }
	    final List<Set<String>> output = new ArrayList<Set<String>>();
	    final Iterator<String> iter = input.iterator();
	    while (iter.hasNext()) {
	        final String item = iter.next();
	        final Set<String> copy = new LinkedHashSet<String>(input);
	        copy.remove(item);
	        for (final Set<String> rest : permutations(copy)) {
	            rest.add(item);
	            output.add(rest);
	        }
	    }
	    return output;
	}
	

}
